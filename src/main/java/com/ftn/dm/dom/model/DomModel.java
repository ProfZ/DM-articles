package com.ftn.dm.dom.model;

import java.util.ArrayList;

/**
 * @author - a.pejakovic
 */
public class DomModel {

    private String tag;
    private String classes;
    private String id;
    private ArrayList<DomModel> children;
    private DomModel parent;
    private int content_size = 0;

    public DomModel() {
        tag = "";
        classes = "";
        id = "";
        children = new ArrayList<>();
        parent = null;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<DomModel> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<DomModel> children) {
        this.children = children;
    }

    public DomModel getParent() {
        return parent;
    }

    public void setParent(DomModel parent) {
        this.parent = parent;
    }

    public int getContent_size() {
        return content_size;
    }

    public void setContent_size(int content_size) {
        this.content_size = content_size;
    }
}
