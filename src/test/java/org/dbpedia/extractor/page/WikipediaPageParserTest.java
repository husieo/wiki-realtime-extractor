package org.dbpedia.extractor.page;

import com.google.common.io.Resources;
import lombok.extern.log4j.Log4j;
import org.dbpedia.exception.ParsingException;
import org.dbpedia.extractor.entity.Subdivision;
import org.dbpedia.extractor.entity.WikiPage;
import org.dbpedia.extractor.service.WikipediaPageParser;
import org.dbpedia.extractor.service.transformer.ContextLanguageTransformer;
import org.dbpedia.extractor.service.transformer.XmlTransformer;
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
    private static XmlTransformer contextLanguageTransformer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        pageParser = new WikipediaPageParser(new ContextLanguageTransformer());

        URL textUrl = Resources.getResource("page_test.txt");
        wikiPage = new WikiPage("Anarchism",
                Resources.toString(textUrl, StandardCharsets.UTF_8));
    }

    @Test
    public void parseParagraphsTest() throws IOException, ParsingException {
        Subdivision root = pageParser.buildPageStructure(wikiPage);
        // check that the paragraphs are parsed
        assertTrue(root.getParagraphs().size() > 1);
        // check that the page has a meaningful structure
        assertTrue(root.getChildren().size() > 1);
    }

    @Test
    public void parseSubdivisionsTest() throws ParsingException {
        Subdivision root = pageParser.buildPageStructure(wikiPage);
        root.logSubdivisionTree();
    }
}