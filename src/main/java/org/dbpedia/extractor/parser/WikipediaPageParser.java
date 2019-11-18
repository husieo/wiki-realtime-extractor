package org.dbpedia.extractor.parser;

import org.dbpedia.extractor.loader.PageLoader;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WikipediaPageParser {

    private String lineSeparator = System.getProperty("line.separator");

    @Autowired
    private PageLoader pageLoader;

    public ParsedPage parsePage(String title) throws IOException {
        ParsedPage result = new ParsedPage();
        result.setTitle(title);
        pageLoader.readPage(title);
        return result;
    }
}
