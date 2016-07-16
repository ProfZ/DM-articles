package com.ftn.dm.dom.app;

import com.ftn.dm.dom.analise.DomAnalise;
import com.ftn.dm.dom.analise.DomSiteMapAnalise;
import com.ftn.dm.dom.config.ConfigLoader;
import com.ftn.dm.dom.config.Type;
import com.ftn.dm.dom.neo4j.DomSiteNeo4jIntegration;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author - a.pejakovic
 */
public class DomAnaliseApp {

    private static final Logger logger = Logger.getLogger(DomAnaliseApp.class.getName());

    private static final DomSiteMapAnalise domSiteMapAnalise = new DomSiteMapAnalise();
    private static final DomAnalise domAnalise = new DomAnalise();

    public static void main(String[] args) {

        String url = ConfigLoader.getUrl();
        if (url == null || url.isEmpty()) {
            logger.log(Level.SEVERE, "Provide url to analise in config file");
            System.exit(-1);
        }

        Type type = ConfigLoader.getType();
        if (type == null || type == Type.ALL) {
            logger.log(Level.INFO, "Starting full analise");
            fullAnalise(url);
        } else if (type == Type.SITE_MAP) {
            logger.log(Level.INFO, "Starting site map analise");
            siteMapOnly(url);
        } else if (type == Type.SITE_MAP_NEO4J) {
            siteMapNeo4j(url);
        }
    }

    private static void siteMapOnly(String url) {
        domSiteMapAnalise.start(url);
    }

    private static void siteMapNeo4j(String url) {
        domSiteMapAnalise.start(url);
        DomSiteNeo4jIntegration.publishSiteMapItemsToNeo(domSiteMapAnalise.getDomSiteMapItems());
    }

    private static void fullAnalise(String url) {
        DomSiteNeo4jIntegration.setUpUniques();
        domAnalise.setUpStartingUrl(url);

        //TODO find out why there are nulls

        domAnalise.analiseSiteMapItem(domSiteMapAnalise, url, ConfigLoader.getTransitive());
    }
}
