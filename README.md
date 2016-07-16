# DM-articles (Generic crawler integration with Neo4j)

## Configuration and startup:

To start DM-articles analise populate properties file and run /src/main/java/com/ftn/dm/dom/app/DomAnaliseApp.java.

Properties file located in: /src/main/resources is used to configure application. It contains next properties:
```
dom-analise.depth - maximum depth for DOM element containing content
dom-analise.candidates - not jet uset
dom-analise.language - default language
dom-analise.type - type of analise [all = full analise, site_map = listing links from sitemap.xml, site_map_neo4j = list links from sitemap.xml and publish to neo4j]

dom-analise.url - starting url for analise, like: http://www.tomshardware.com/
dom-analise.threads - number of threads used in analise
dom-analise.sources-depth - maximum depth of sources for page sources

dom-analise.transitive - are sources gonna be analised 

dom-site-map.limit - limit of starting urls from sitemap.xml
dom-site-map.site - default sitemap.xml location for chosen site
dom-site-map.robots - default robots.txt location fro chosen site

dom-neo4j.url - neo4j server location
dom-neo4j.username - username for neo4j server
dom-neo4j.password - password for neo4j server
```

## How it works:

DomAnaliseApp first locates sitemap.xml for provided site, uses robots.txt file for that. Next step is retrieving links from sitemap.xml site, links represent url locations to html pages containing news. After that, DomAnaliseApp in each page searches for DOM element that is most likely to contain content. DomAnaliseApp also collects other public info from page, like name of the author, date of publishing, title, etc. After finding content element, DomAnaliseApp tries to find links that are mentioned like source urls for content. If **dom-analise.transitive** property is set to **true** then found source links will be analised in same way. 
Results of analise are stored in Neo4j database in form: (page) - [is_source] -> (page).

## Useful queries

To retrieve set number of elements (pages) and to see connections between them:
```
MATCH (n:Page) RETURN n LIMIT 25
```

To see all pages from one author, with other pages that use found page as source, up to desired depth:
```
MATCH (n:Page {author:"@chris_angelini"}) MATCH (m:Page) MATCH (n)-[:USE_SOURCE*..1]-(m) RETURN m LIMIT 100
```

To sort most connected pages published on date:
```
MATCH (n:Page {date:"2016-07-11"}) RETURN n, length((n)--()) AS connectedness ORDER BY connectedness DESC LIMIT 30;
```

To get relationships:
```
MATCH p=()-[r:USE_SOURCE]->() RETURN p LIMIT 25
```

To delete all - kill it with fire:
```
MATCH (n) DETACH DELETE n
```

Note that all queries return result in 4 different types: graph, code, text and rows. Result of every query can be exported as JSON and CSV using [export to file button] located as most left button in the top right corner of the resul query window.

Data analise of results and integration with data anlise tools is in progress.
