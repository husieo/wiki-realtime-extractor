package org.dbpedia.extractor.page;

import com.google.common.io.Resources;
import lombok.extern.java.Log;
import org.dbpedia.extractor.service.WikipediaPageParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Log
class WikipediaPageParserTest {

    private static WikipediaPageParser pageParser;
    private static WikiPage wikiPage;

    @BeforeAll
    static void beforeAll() throws IOException {
        pageParser = new WikipediaPageParser();

        URL textUrl = Resources.getResource("page_test.txt");
        wikiPage = new WikiPage("test",
                Resources.toString(textUrl, StandardCharsets.UTF_8));
    }

    @Test
    void parsePage() throws IOException {
        ParsedPage parsedPage = pageParser.parsePage(wikiPage);
        log.info(parsedPage.getParagraphs().toString());
        assertTrue(parsedPage.getParagraphs().size() > 1);
    }
}