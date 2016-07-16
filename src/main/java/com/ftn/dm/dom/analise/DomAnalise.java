package com.ftn.dm.dom.analise;

import com.cybozu.labs.langdetect.Detector;
import com.ftn.dm.dom.config.ConfigLoader;
import com.ftn.dm.dom.model.DomPathValueModel;
import com.ftn.dm.dom.model.DomSiteMapItem;
import com.ftn.dm.dom.neo4j.DomSiteNeo4jIntegration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author - a.pejakovic
 */
public class DomAnalise {

    private static final Logger logger = Logger.getLogger(DomAnalise.class.getName());

    private static final String[] __body_tags = {
            "article", "content", "post", "container", "main", "story", "text", "body"
    };

    private static final String[] __ad_tags = {
            "ad", "badge", "hidden", "plugin", "sidebar"
    };

    private static final String[] __skip_tags = {
            "script", "style", "noscript"
    };

    private static final String[] __skip_urls = {
            "twitter", "facebook", "instagram", "google", "youtube", "pinterest", "youtu.be", "walmart"
    };

    private final int __dom_lvl;
    private final int __dom_cand;
    private Detector __detector;
    private String baseUrl = "";
    private final int threadCount;
    private final int sourcesDepth;

    public DomAnalise() {
        super();
        this.__dom_lvl = ConfigLoader.getDepth();
        this.__dom_cand = ConfigLoader.getCandidates();
        this.threadCount = ConfigLoader.getThreads();
        this.sourcesDepth = ConfigLoader.getSourcesDepth();
        // TODO make language detector
    }

    public Element getContentElement(Element element) {
        Map<String, Integer> allElements = allElements(element, 0);

        Optional option = allElements.keySet().stream().findFirst();
        String candidate = "";
        if (option.isPresent()) {
            candidate = option.get().toString();
            Integer value = allElements.get(candidate);
            for (Map.Entry<String, Integer> entry : allElements.entrySet()) {
                if (entry.getValue() > value) {
                    candidate = entry.getKey();
                    value = entry.getValue();
                }
            }
        }

        if (candidate.isEmpty())
            return element;

        return element.select(candidate).first();
    }

    private Map<String, Integer> allElements(Element startElement, int startDepth) {
        Map<String, Integer> retVal = new HashMap<>();

        for (Element child : startElement.children()) {
            boolean skip = false;
            for (String skipTag : __skip_tags) {
                if (child.tagName().equals(skipTag)) {
                    skip = true;
                    break;
                }
            }
            if (skip) continue;

            Integer value = startDepth * startDepth * child.text().length();
            value *= (int) pPathElement(child, new DomPathValueModel()).getValue();
            try {
                retVal.put(startElement.cssSelector(), value);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getLocalizedMessage());
                continue;
            }

            if (startDepth < __dom_lvl) {
                retVal.putAll(allElements(child, startDepth + 1));
            }
        }

        return retVal;
    }

    private DomPathValueModel pPathElement(Element element, DomPathValueModel old_str) {
        DomPathValueModel retVal = new DomPathValueModel();

        String tag = element.tagName();
        String classes = element.className();
        String id = element.id();
        String p_path = retVal.getpPath();
        if (classes.length() > 0) {
            p_path += tag + "." + classes.replaceAll(" ", ".");
        } else if (id.length() > 0) {
            p_path += tag + "#" + id;
        } else {
            p_path += tag;
        }
        double value = retVal.getValue();
        for (String body : __body_tags) {
            if (p_path.contains(body)) {
                value *= 1.2;
            }
        }
        for (String ad : __ad_tags) {
            if (p_path.contains(ad)) {
                value *= 0.8;
            }
        }
        retVal.setpPath(p_path + " " + old_str.getpPath());
        retVal.setValue(value * old_str.getValue());
        if (element.parent() != null && !element.parent().tagName().equals("")) {
            return pPathElement(element.parent(), retVal);
        }

        return retVal;
    }

    public void analiseSiteMapItem(DomSiteMapAnalise analise, String url, boolean isTransitive) {
        analise.start(url);
        List<List<DomSiteMapItem>> domSiteMapItemList = new ArrayList<>();
        List<DomSiteMapItem> domSiteMapItems = analise.getDomSiteMapItems();
        for (int i = 0; i < this.threadCount; ++i) {
            domSiteMapItemList.add(new ArrayList<>());
        }

        for (int i = 0; i < domSiteMapItems.size(); ++i) {
             domSiteMapItemList.get(i % this.threadCount).add(domSiteMapItems.get(i));
        }

        List<Callable<Integer>> callables = domSiteMapItemList
                .stream()
                .map(items -> callable(isTransitive, items))
                .collect(Collectors.toList());

        ExecutorService executorService = Executors.newWorkStealingPool(this.threadCount);

        try {
            executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "WE FUCKED UP");
        }

    }

    Callable<Integer> callable(boolean isTransitive, List<DomSiteMapItem> domSiteMapItems) {
        return () -> {
            for (DomSiteMapItem domSiteMapItem : domSiteMapItems) {
                DomSiteMapItem dsmi = this.analiseSiteMapItem(domSiteMapItem.getLink(), isTransitive, domSiteMapItem, 0);
                DomSiteNeo4jIntegration.publishFullSiteMapItem(dsmi);
            }
            return 0;
        };
    }

    public DomSiteMapItem analiseSiteMapItem(String link, boolean isTransitive, DomSiteMapItem domSiteMapItem, int sourceDepth) {
        if (link == null || link.equals("") || link.trim().equals("null") || !linkIsSource(link.trim()) || sourceDepth > this.sourcesDepth) {
            return null;
        }

        try {
            Document document = Jsoup.connect(link).get();
            Element body = document.body();
            Element articleElement = getContentElement(body);
            domSiteMapItem = createOrUpdateDSMI(domSiteMapItem, document);
            domSiteMapItem.setLink(link);

            if (articleElement == null) return null; // we don't need articles without content

            Elements links = articleElement.select("a");
            Elements images = articleElement.select("img");
            long contentSize = articleElement.text().length();

            domSiteMapItem.setContentSize(contentSize);
            domSiteMapItem.setNumberOfImages(images.size());

            List<String> sources = new ArrayList<>();

            ++sourceDepth;
            if (isTransitive) {
                for (Element elementLink : links) {
                    DomPathValueModel path = pPathElement(elementLink, new DomPathValueModel());
                    if (containsAdSignature(path)) {
                        continue;
                    }

                    String url = elementLink.attr("href");
                    if (url.startsWith("/")) {
                        url = this.baseUrl + url;
                    }

                    //TODO add white list for urls

                    try {
                        DomSiteMapItem dsmi = analiseSiteMapItem(url, isTransitive, null, sourceDepth);
                        if (dsmi != null) {
                            sources.add(url);
                            DomSiteNeo4jIntegration.publishFullSiteMapItem(dsmi);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, e.getLocalizedMessage());
                    }
                }
                domSiteMapItem.setSources(sources.toArray(new String[0]));
            } else {
                sources = new ArrayList<>(links.size());
                sources.addAll(links.stream().map(elementLink -> elementLink.attr("href")).collect(Collectors.toList()));
                domSiteMapItem.setSources(sources.toArray((new String[0])));
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
        }

        logger.log(Level.INFO, "DONE: " + domSiteMapItem.getLink());
        return domSiteMapItem;
    }

    private boolean containsAdSignature(DomPathValueModel path) {
        String pathString = path.getpPath();
        for (String addBaner : __ad_tags) {
            if (pathString.contains(addBaner)) {
                return true;
            }
        }
        return false;
    }

    private boolean linkIsSource(String elementLink) {
        if (elementLink == null || elementLink.equals("") || elementLink.equals("null")) {
            return false;
        } else if (elementLink.startsWith("https://")) {
            elementLink = elementLink.replace("https://", "").trim();
        } else if (elementLink.startsWith("http://")) {
            elementLink = elementLink.replace("http://", "").trim();
        } else if (elementLink.startsWith("#")) {
            return false;
        } else if (elementLink.endsWith("/")) {
            return false;
        }

        if (elementLink.indexOf('/') > 1)
            elementLink = elementLink.substring(0, elementLink.indexOf('/'));

        for (String url : __skip_urls) {
            if (elementLink.contains(url)) {
                return false;
            }
        }
        return true;
    }

    private DomSiteMapItem createOrUpdateDSMI(DomSiteMapItem domSiteMapItem, Document document) {
        if (domSiteMapItem == null) {
            domSiteMapItem = new DomSiteMapItem();
        }

        boolean descriptionFound = false;
        boolean typeFound = false;
        boolean fbAppIpFound = false;
        boolean twitterSiteFound = false;
        boolean twitterCreatorFound = false;
        boolean titleFound = false;
        boolean siteNameFound = false;
        //boolean keywordsFound = false;
        //boolean languageFound = false;
        boolean thumbnailUrlFound = false;
        boolean locationFound = false;

        String description = "";
        String fbAppIp = "";
        String type = "";
        String language = "en";
        String twitterSite = "";
        String twitterCreator = "";
        String title = "";
        String location = "";
        String thumbnailUrl = "";
        String siteName = "";
        Set<String> keywordsSet = new HashSet<>();
        String keywords = "";

        Element head = document.head();
        for (Element child : head.children()) {
            if (!titleFound && child.tagName().equals("title")) {
                title = child.text();
                titleFound = true;
            } else {
                if (!descriptionFound) {
                    if (child.hasAttr("itemprop") && child.attr("itemprop").equals("description")) {
                        description = child.attr("name");
                        descriptionFound = true;
                        continue;
                    } else if (child.hasAttr("property") && (child.attr("property").equals("og:description") || child.attr("property").equals("twitter:description"))) {
                        description = child.attr("content");
                        descriptionFound = true;
                        continue;
                    }
                }

                if (!fbAppIpFound) {
                    if (child.hasAttr("property") && child.attr("property").equals("fb:app_id")) {
                        fbAppIp = child.attr("content");
                        fbAppIpFound = true;
                        continue;
                    }
                }

                if (!typeFound) {
                    if (child.hasAttr("property") && child.attr("property").equals("og:type")) {
                        type = child.attr("content");
                        typeFound = true;
                        continue;
                    } else if (child.hasAttr("itemprop") && child.attr("itemprop").equals("genre")) {
                        type = child.attr("content");
                        typeFound = true;
                        continue;
                    }
                }

                if (!twitterSiteFound && child.hasAttr("name") && child.attr("name").equals("twitter:site")) {
                    twitterSite = child.attr("content");
                    twitterSiteFound = true;
                    continue;
                }

                if (!twitterCreatorFound) {
                    if (child.hasAttr("name") && child.attr("name").equals("twitter:creator")) {
                        twitterCreator = child.attr("content");
                        twitterCreatorFound = true;
                        continue;
                    } else if (child.hasAttr("property") && child.attr("property").equals("nv:author")) {
                        twitterCreator = child.attr("content");
                        twitterCreatorFound = true;
                        continue;
                    }
                }

                if (!locationFound && child.hasAttr("property") && child.attr("property").equals("og:locale")) {
                    location = child.attr("content");
                    locationFound = true;
                    continue;
                }

                if (!siteNameFound && child.hasAttr("property") && child.attr("property").equals("og:site_name")) {
                    siteName = child.attr("content");
                    siteNameFound = true;
                    continue;
                }

                if (child.hasAttr("name") && child.attr("name").contains("keywords")) {
                    String temp = child.attr("content");
                    for (String tempS : temp.split(",")) {
                        keywordsSet.add(tempS);
                    }
                    continue;
                } else if (child.hasAttr("property") && child.attr("property").contains("tags")) {
                    String temp = child.attr("content");
                    for (String tempS : temp.split(",")) {
                        keywordsSet.add(tempS);
                    }
                    continue;
                }

                if (!thumbnailUrlFound) {
                    if ((child.hasAttr("name") && child.attr("name").equals("twitter:image")) || (child.hasAttr("property") && child.attr("property").equals("og:image"))) {
                        thumbnailUrl = child.attr("content");
                        thumbnailUrlFound = true;
                        continue;
                    }
                }
            }
        }

        if (domSiteMapItem.getNewsLanguage() == null) {
            domSiteMapItem.setNewsLanguage(language);
        }

        if (domSiteMapItem.getTitle() == null) {
            domSiteMapItem.setTitle(title);
        }

        if (domSiteMapItem.getAuthor() == null) {
            domSiteMapItem.setAuthor(twitterCreator);
        }

        if (domSiteMapItem.getKeywords() == null) {
            domSiteMapItem.setKeywords(keywordsSet.toArray(new String[0]));
        }

        if (domSiteMapItem.getNewsName() == null) {
            domSiteMapItem.setNewsName(siteName);
        }

        // TODO add all found common meta data to domSiteMapItem
        return domSiteMapItem;
    }

    public void setUpStartingUrl(String url) {
        int startingPossition = url.indexOf("://");
        int firstSlash = url.indexOf('/', startingPossition);
        this.baseUrl = url.substring(0, firstSlash);
    }
}
