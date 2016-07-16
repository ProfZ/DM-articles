package com.ftn.dm.dom.neo4j;

import com.ftn.dm.dom.config.ConfigLoader;
import com.ftn.dm.dom.model.DomSiteMapItem;
import org.neo4j.driver.v1.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author - a.pejakovic
 */
public class DomSiteNeo4jIntegration {

    private static final Logger logger = Logger.getLogger(DomSiteNeo4jIntegration.class.getName());

    private static final String neo4jLocation = ConfigLoader.getNeo4jSite();
    private static final String neo4jUsername = ConfigLoader.getNeo4jUsername();
    private static final String neo4jPassword = ConfigLoader.getNeo4jPassword();

    private static final AuthToken token;
    private static final Driver driver;

    static {
        if (neo4jLocation == null || neo4jLocation.isEmpty()) {
            logger.log(Level.SEVERE, "No neo4j url is provided.");
            System.exit(-1);
        }

        token = AuthTokens.basic(neo4jUsername, neo4jPassword);
        driver = GraphDatabase.driver(neo4jLocation, token);

    }

    public static void publishSiteMapItemsToNeo(List<DomSiteMapItem> siteMapItems) {
        Session session = driver.session();

        StringBuilder stringBuilder = new StringBuilder();

        for (DomSiteMapItem item : siteMapItems) {

            stringBuilder.append("CREATE (a:Page {url:'").append(item.getLink()).append("',");
            stringBuilder.append(" title:'").append(item.getTitle()).append("',");
            stringBuilder.append(" imageLink:'").append(item.getImageLink()).append("',");
            stringBuilder.append(" date:'").append(item.getDate()).append("',");
            stringBuilder.append(" keywords:'").append(item.getKeywordsString()).append("',");
            stringBuilder.append(" author:'").append(item.getAuthor()).append("',");
            stringBuilder.append(" name:'").append(item.getNewsName()).append("',");
            stringBuilder.append(" language:'").append(item.getNewsLanguage()).append("'})");

            session.run(stringBuilder.toString());

            stringBuilder.setLength(0);
        }

        session.close();
        driver.close();
    }

    public static void publishFullSiteMapItem(DomSiteMapItem item) {
        Session session = driver.session();

        String preQuery = "MATCH (p:Page { url:\"" + item.getLink() + "\" })\n RETURN p";
        StatementResult result = session.run(preQuery);

        StringBuilder stringBuilder = new StringBuilder();

        if (!result.hasNext()) {
            stringBuilder.append("CREATE (a:Page {url:'").append(item.getLink()).append("',");
            stringBuilder.append(" title:'").append(item.getTitle()).append("',");
            stringBuilder.append(" imageLink:'").append(item.getImageLink()).append("',");
            stringBuilder.append(" numberOfImages: '").append(item.getNumberOfImages()).append("',");
            stringBuilder.append(" date:'").append(item.getDate()).append("',");
            stringBuilder.append(" keywords:'").append(item.getKeywordsString()).append("',");
            stringBuilder.append(" author:'").append(item.getAuthor()).append("',");
            stringBuilder.append(" name:'").append(item.getNewsName()).append("',");
            stringBuilder.append(" language:'").append(item.getNewsLanguage()).append("'})");

            session.run(stringBuilder.toString());

            stringBuilder.setLength(0);
        }

        if (item.getSources() != null)
            for (String url : item.getSources()) {
                url = url.replaceAll("'", "_");

                String query = stringBuilder.append("MATCH (u:Page {url:'").append(item.getLink())
                        .append("'}), (r:Page {url:'").append(url).append("'}) CREATE (u)-[:USE_SOURCE]->(r) ")
                        .toString();

                session.run(query);
                stringBuilder.setLength(0);
            }

        session.close();
        driver.close();
    }

    @Deprecated
    public static void createIndexes() {
        Session session = driver.session();

        String query = "CREATE INDEX ON :Page(url)";

        session.run(query);

        session.close();
        driver.close();
    }


    public static void setUpUniques() {
        Session session = driver.session();

        String query = "CREATE CONSTRAINT ON (p:Page) ASSERT p.url IS UNIQUE\n";

        session.run(query);

        session.close();
        driver.close();
    }
}
