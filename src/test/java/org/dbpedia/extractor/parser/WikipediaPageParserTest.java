package org.dbpedia.extractor.parser;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import lombok.extern.apachecommons.CommonsLog;
import lombok.extern.log4j.Log4j;
import org.dbpedia.extractor.ResourceExtractor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CommonsLog
class WikipediaPageParserTest {

    private WikipediaPageParser parser = new WikipediaPageParser();

    private String testParagraphs = ResourceExtractor.asString("maradona_extract_raw.txt");

    WikipediaPageParserTest() throws IOException {
    }

    @Test
    void parseParagraphs() {
        List<String> paragraphs = parser.parseParagraphs(testParagraphs);
        for(String paragraph : paragraphs){
            log.info(paragraph);
        }
        assertEquals(3,paragraphs.size());
    }
}