package org.dbpedia.extractor.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.dbpedia.extractor.entity.ParsedPage;
import org.dbpedia.extractor.entity.WikiPage;
import org.dbpedia.extractor.entity.xml.Mediawiki;
import org.dbpedia.extractor.entity.xml.Page;
import org.dbpedia.extractor.entity.xml.Revision;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class XmlDumpParser {

    private final WikipediaPageParser wikipediaPageParser;

    public List<WikiPage> pageList;

    public XmlDumpParser(WikipediaPageParser wikipediaPageParser) {
        this.wikipediaPageParser = wikipediaPageParser;
    }

    public List<ParsedPage> parseXmlDump(String xmlDump) throws IOException {
        Mediawiki mediawiki = deserializeXml(xmlDump);
        List<Page> pages = mediawiki.getPages();
        List<ParsedPage> parsedPages = new ArrayList<>();
        for (Page page : pages) {
            Revision revision = page.getRevision();
            WikiPage wikiPage = new WikiPage(page.getTitle(), revision.getText());
            parsedPages.add(wikipediaPageParser.parsePage(wikiPage));
        }
        return parsedPages;
    }

    private Mediawiki deserializeXml(String dump) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return xmlMapper.readValue(dump, Mediawiki.class);
    }
}
