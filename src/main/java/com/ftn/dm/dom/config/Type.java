package com.ftn.dm.dom.config;

/**
 * @author - a.pejakovic
 */
public enum Type {
    ALL("ALL"),
    SITE_MAP("SITE_MAP"),
    SITE_MAP_NEO4J("SITE_MAP_NEO4J");

    private final String stringValue;

    Type(String s) {
        this.stringValue = s;
    }

    public String toString() {
        return stringValue;
    }
}
