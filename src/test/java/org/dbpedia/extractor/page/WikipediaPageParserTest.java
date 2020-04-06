package org.dbpedia.extractor.page;

import com.google.common.io.Resources;
import lombok.extern.log4j.Log4j;
import org.dbpedia.extractor.entity.ParsedPage;
import org.dbpedia.extractor.entity.Subdivision;
import org.dbpedia.extractor.entity.WikiPage;
import org.dbpedia.extractor.service.WikipediaPageParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

@Log4j
public class WikipediaPageParserTest {

    private static WikipediaPageParser pageParser;
    private static WikiPage wikiPage;

    @BeforeAll
    public static void beforeAll() throws IOException {
        pageParser = new WikipediaPageParser();

        URL textUrl = Resources.getResource("page_test.txt");
        wikiPage = new WikiPage("Anarchism",
                Resources.toString(textUrl, StandardCharsets.UTF_8));
    }

    @Test
    public void parseParagraphsTest() throws IOException {
        Subdivision root = pageParser.buildPageStructure(wikiPage);
        assertTrue(root.getParagraphs().size() > 1);
    }

    @Test
    public void parseSubdivisionsTest(){
        Subdivision root = pageParser.buildPageStructure(wikiPage);
        root.logSubdivisionTree();
    }
}