package com.ftn.dm.dom.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author - a.pejakovic
 */
public class ConfigLoader {

    private static final Logger logger = Logger.getLogger(ConfigLoader.class.getName());

    private static final String DOM_PREFIX = "dom-analise.";
    private static final String MAX_DEPTH = "depth";
    private static final String DEFAULT_MAX_DEPTH = "10";
    private static final String MAX_CANDIDATES = "candidates";
    private static final String DEFAULT_MAX_CANDIDATES = "4";
    private static final String LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String TYPE = "type";
    private static final String DEFAULT_TYPE = "all";
    private static final String URL = "url";
    private static final String TRANSITIVE = "transitive";
    private static final String DEFAULT_TRANSITIVE = "false";
    private static final String THREADS = "threads";
    private static final String DEFAULT_THREADS = "10";
    private static final String SOURCES_DEPTH = "sources-depth";
    private static final String DEFAULT_SOURCES_DEPTH = "2";

    private static final String SITE_MAP_PREFIX = "dom-site-map.";
    private static final String LIMIT = "limit";
    private static final String DEFAULT_LIMIT = "100";
    private static final String SITE = "site";
    private static final String DEFAULT_SITE = "sitemap.xml";
    private static final String ROBOTS = "robots";
    private static final String DEFAULT_ROBOTS = "robots.txt";

    private static final String NEO4J_PREFIX = "dom-neo4j.";
    private static final String USERNAME = "username";
    private static final String DEFAULT_USERNAME = "neo4j";
    private static final String PASSWORD = "password";
    private static final String DEFAULT_PASSWORD = "neo4j";

    private static Properties domAnaliseProperties;

    static {
        domAnaliseProperties = new Properties();

        try (InputStream input = new FileInputStream("src/main/resources/dom-analise.properties")) {
            domAnaliseProperties.load(input);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    public static Properties getDomAnaliseProperties() {
        return domAnaliseProperties;
    }

    public static int getDepth() {
        return Integer.parseInt(domAnaliseProperties.getProperty(DOM_PREFIX + MAX_DEPTH, DEFAULT_MAX_DEPTH));
    }

    public static int getCandidates() {
        return Integer.parseInt(domAnaliseProperties.getProperty(DOM_PREFIX + MAX_CANDIDATES, DEFAULT_MAX_CANDIDATES));
    }

    public static String getLanguage() {
        return domAnaliseProperties.getProperty(DOM_PREFIX + LANGUAGE, DEFAULT_LANGUAGE);
    }

    public static Type getType() {
        return Type.valueOf(domAnaliseProperties.getProperty(DOM_PREFIX + TYPE, DEFAULT_TYPE).toUpperCase());
    }

    public static String getUrl() {
        return domAnaliseProperties.getProperty(DOM_PREFIX + URL);
    }

    public static int getLimit() {
        return Integer.parseInt(domAnaliseProperties.getProperty(SITE_MAP_PREFIX + LIMIT, DEFAULT_LIMIT));
    }

    public static String getSite() {
        return domAnaliseProperties.getProperty(SITE_MAP_PREFIX + SITE, DEFAULT_SITE);
    }

    public static String getRobots() {
        return domAnaliseProperties.getProperty(SITE_MAP_PREFIX + ROBOTS, DEFAULT_ROBOTS);
    }

    public static String getNeo4jSite() {
        return domAnaliseProperties.getProperty(NEO4J_PREFIX + URL);
    }

    public static String getNeo4jUsername() {
        return domAnaliseProperties.getProperty(NEO4J_PREFIX + USERNAME, DEFAULT_USERNAME);
    }

    public static String getNeo4jPassword() {
        return domAnaliseProperties.getProperty(NEO4J_PREFIX + PASSWORD, DEFAULT_PASSWORD);
    }

    public static boolean getTransitive() {
        return Boolean.parseBoolean(domAnaliseProperties.getProperty(DOM_PREFIX + TRANSITIVE, DEFAULT_TRANSITIVE));
    }

    public static int getThreads() {
        return Integer.parseInt(domAnaliseProperties.getProperty(DOM_PREFIX + THREADS, DEFAULT_THREADS));
    }

    public static int getSourcesDepth() {
        return Integer.parseInt(domAnaliseProperties.getProperty(DOM_PREFIX + SOURCES_DEPTH, DEFAULT_SOURCES_DEPTH));
    }
}
