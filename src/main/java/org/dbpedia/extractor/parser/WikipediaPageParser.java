package org.dbpedia.extractor.parser;

import org.dbpedia.extractor.loader.PageLoader;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WikipediaPageParser {

    private static Pattern paragraphBreakPattern = Pattern.compile("\\r?\\n\\n");

    private final PageLoader pageLoader;

    public WikipediaPageParser(PageLoader pageLoader) {
        this.pageLoader = pageLoader;
    }

    public ParsedPage parsePage(String title) throws IOException {
        ParsedPage result = new ParsedPage();
        result.setTitle(title);
        Document document = pageLoader.readPage(title);
        Element docBody = document.body();
        String docText = docBody.text();
        docText = removeInitialInformation(docText);

        result.setParagraphs(parseParagraphs(docText));
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
