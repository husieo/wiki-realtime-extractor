package org.dbpedia.extractor.service;

import org.dbpedia.extractor.page.ParsedPage;
import org.dbpedia.extractor.page.WikiPage;
import org.dbpedia.extractor.xml.XmlParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WikipediaPageParser {

    private static Pattern paragraphBreakPattern = Pattern.compile("\\r?\\n\\n");
    private static Pattern initialInformationPattern = Pattern.compile("(\\{\\{.*\\}\\})");

    public ParsedPage parsePage(WikiPage page) throws IOException {
        ParsedPage result = new ParsedPage();
        result.setTitle(page.getTitle());
        String text = page.getText();
        result.setParagraphs(parseParagraphs(text));
        return result;
    }

    /**
     * @param text wiki article
     * @return list of paragraphs
     */
    List<String> parseParagraphs(String text){
        return Arrays.asList(paragraphBreakPattern.split(text));
    }

}
