package com.ftn.dm.dom.analise;

import com.ftn.dm.dom.config.ConfigLoader;
import com.ftn.dm.dom.model.DomSiteMapItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author - a.pejakovic
 */
public class DomSiteMapAnalise {

    private static final Logger logger = Logger.getLogger(DomSiteMapAnalise.class.getName());

    private static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder documentBuilder;

    private int limit;
    private List<DomSiteMapItem> siteMapItems;
    private List<String> siteMaps;

    private void getDSMIForSite() {
        this.siteMapItems = new ArrayList<>();
        for (String siteMap : siteMaps) {
            URL siteUrl;
            Document document;
            try {
                siteUrl = new URL(siteMap);
                document = documentBuilder.parse(siteUrl.openStream());
                NodeList nodeList = document.getElementsByTagName("url");
                int i = 0;
                if (nodeList.getLength() != 0) {
                    for (; i < nodeList.getLength() && i < limit; ++i) {
                        siteMapItems.add(collectNodeInfo(nodeList.item(i)));
                    }
                } else {
                    nodeList = document.getElementsByTagName("loc");
                    for (; i < nodeList.getLength() && i < limit; ++i) {
                        Node node = nodeList.item(i);
                        DomSiteMapItem domSiteMapItem = new DomSiteMapItem();
                        domSiteMapItem.setLink(node.getNodeValue());
                        siteMapItems.add(domSiteMapItem);
                    }
                }
                logger.log(Level.INFO, "Found: " + i + " elements");
            } catch (SAXException | IOException e) {
                logger.log(Level.INFO, String.format("Exception with url: %s, error: %s", siteMap, e.getMessage()));
            }
        }
    }

    private DomSiteMapItem collectNodeInfo(Node item) {
        DomSiteMapItem domSiteMapItem = new DomSiteMapItem();

        if (item.getNodeType() == Node.ELEMENT_NODE) {
            NodeList children = item.getChildNodes();

            for (int i = 0; i < children.getLength(); ++i) {
                Node node = children.item(i);
                String nodeName = node.getNodeName();

                switch (nodeName) {
                    case "loc":
                        domSiteMapItem.setLink(node.getTextContent());
                        break;
                    case "news:news":
                        NodeList newsChildNodes = node.getChildNodes();
                        for (int j = 0; j < newsChildNodes.getLength(); ++j) {
                            Node newsNode = newsChildNodes.item(j);
                            String newsNodeName = newsNode.getNodeName();

                            switch (newsNodeName) {
                                case "news:publication":
                                    NodeList publicationNodeList = newsNode.getChildNodes();
                                    for (int k = 0; k < publicationNodeList.getLength(); ++k) {
                                        Node publicationNode = publicationNodeList.item(k);
                                        String publicationNodeName = publicationNode.getNodeName();

                                        if (publicationNodeName.equals("news:name")) {
                                            domSiteMapItem.setNewsName(publicationNode.getTextContent());
                                        } else if (publicationNodeName.equals("news:language")) {
                                            domSiteMapItem.setNewsLanguage("en");
                                        }
                                    }
                                    break;
                                case "news:publication_date":
                                    domSiteMapItem.setDate(newsNode.getTextContent());
                                    break;
                                case "news:title":
                                    domSiteMapItem.setTitle(newsNode.getTextContent());
                                    break;
                                case "news:keywords":
                                    String[] keywords = newsNode.getTextContent().replaceAll(" ", "").split(",");
                                    domSiteMapItem.setKeywords(keywords);
                                    break;
                            }
                        }
                        break;
                    case "image:image":
                        NodeList imageChildrenList = node.getChildNodes();

                        for (int j = 0; j < imageChildrenList.getLength(); ++j) {
                            Node imageNode = imageChildrenList.item(j);
                            String imageNodeName = imageNode.getNodeName();

                            if (imageNodeName.equals("image:loc")) {
                                domSiteMapItem.setImageLink(imageNode.getTextContent());
                            }
                        }
                        break;
                }
            }
        }
        logger.log(Level.INFO, "Added: " + domSiteMapItem.toString());
        return domSiteMapItem;
    }

    public void start(String baseUrl) {
        String[] parts = baseUrl.split("://");
        baseUrl = parts[parts.length - 1];

        String url;
        int index = baseUrl.indexOf('/');
        if (index == -1) {
            url = baseUrl;
        } else {
            url = baseUrl.substring(0, index);
        }
        url = "http://" + url + "/";

        this.limit = ConfigLoader.getLimit();
        String robotsUrl = ConfigLoader.getRobots();
        String defaultSiteMap = ConfigLoader.getSite();

        this.siteMaps = new ArrayList<>();

        try {
            URL robotUrl = new URL(url + robotsUrl);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(robotUrl.openStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.trim().startsWith("Sitemap:")) {
                        String siteMapUrl = line.replace("Sitemap:", "").trim();
                        if (siteMapUrl.startsWith("http:") || siteMapUrl.startsWith("https:")) {
                            this.siteMaps.add(siteMapUrl);
                        } else {
                            this.siteMaps.add(url + siteMapUrl);
                        }
                    }
                }
                if (this.siteMaps.isEmpty()) {
                    this.siteMaps.add(url + defaultSiteMap);
                }
                documentBuilder = builderFactory.newDocumentBuilder();
                this.getDSMIForSite();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    public List<DomSiteMapItem> getDomSiteMapItems() {
        return siteMapItems;
    }
}
