package org.dbpedia.extractor.page;

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

    public ParsedPage parsePage(WikiPage page) throws IOException {
        ParsedPage result = new ParsedPage();
        result.setTitle(page.getTitle());
        return result;
    }

    /**
     * @param text wiki article
     * @return list of paragraphs
     */
    List<String> parseParagraphs(String text){
        return Arrays.asList(paragraphBreakPattern.split(text));
    }

    String removeInitialInformation(String text){
        Matcher matcher = paragraphBreakPattern.matcher(text);
        int firstOccurence = matcher.start();
        text = text.substring(0, firstOccurence);
        return text;
    }
}
