package com.ftn.dm.dom.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author - a.pejakovic
 */
public class DomSiteMapItem {

    private String link;
    private String date;
    private String title;
    private String[] keywords;
    private String imageLink;
    private String author;
    private String newsName;
    private String newsLanguage;
    private long contentSize;
    private long numberOfImages;
    private String[] sources;

    public DomSiteMapItem() {
        super();
    }

    public DomSiteMapItem(String link, String date, String title, String[] keywords, String imageLink, String author, String newsName, String newsLanguage, long contentSize, long numberOfImages, String[] sources) {
        super();
        this.link = link;
        this.date = date;
        this.title = title;
        this.keywords = keywords;
        this.imageLink = imageLink;
        this.author = author;
        this.newsName = newsName;
        this.newsLanguage = newsLanguage;
        this.contentSize = contentSize;
        this.numberOfImages = numberOfImages;
        this.sources = sources;
    }

    public long getContentSize() {
        return contentSize;
    }

    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }

    public long getNumberOfImages() {
        return numberOfImages;
    }

    public void setNumberOfImages(long numberOfImages) {
        this.numberOfImages = numberOfImages;
    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        this.sources = sources;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        link = link.replaceAll("'", "_");
        this.link = link;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        title = title.replaceAll("'", "_");
        this.title = title;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        List<String> fixedKeywords = new ArrayList<>(keywords.length);

        for (String keyword : keywords) {
            keyword = keyword.replaceAll("'", "_");
            fixedKeywords.add(keyword);
        }

        this.keywords = fixedKeywords.toArray(new String[0]);
    }

    public String getKeywordsString() {
        String retVal = "";
        for (String keyword : this.keywords) {
            retVal += keyword + ", ";
        }
        if (!retVal.isEmpty() && retVal.length() > 3) {
            retVal.substring(0, retVal.length() - 3);
        }
        return retVal;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        imageLink = imageLink.replaceAll("'", "_");
        this.imageLink = imageLink;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getNewsName() {
        return newsName;
    }

    public void setNewsName(String newsName) {
        newsName = newsName.replaceAll("'", "_");
        this.newsName = newsName;
    }

    public String getNewsLanguage() {
        return newsLanguage;
    }

    public void setNewsLanguage(String newsLanguage) {
        this.newsLanguage = newsLanguage;
    }

    @Override
    public String toString() {
        return "DomSiteMapItem{" +
                "link='" + link + '\'' +
                ", date='" + date + '\'' +
                ", title='" + title + '\'' +
                ", keywords=" + Arrays.toString(keywords) +
                ", imageLink='" + imageLink + '\'' +
                ", author='" + author + '\'' +
                ", newsName='" + newsName + '\'' +
                ", newsLanguage='" + newsLanguage + '\'' +
                '}';
    }
}
