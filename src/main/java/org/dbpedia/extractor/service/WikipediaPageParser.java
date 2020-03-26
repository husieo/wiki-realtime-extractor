package org.dbpedia.extractor.service;

import org.dbpedia.extractor.entity.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WikipediaPageParser {

    private static Pattern paragraphBreakPattern = Pattern.compile("\\r?\\n\\n");
    private static Pattern headingPattern = Pattern.compile("=+.+=+\\n");

    public ParsedPage parsePage(WikiPage page) {
        ParsedPage result = new ParsedPage();
        String text = page.getText();
        result.setStructureRoot(buildPageStructure(page));
        return result;
    }

    /**
     * @param text wiki article
     * @return list of paragraphs
     */
    private List<Paragraph> parseParagraphs(String text) {
        Matcher paragraphBreakMatcher = paragraphBreakPattern.matcher(text);
        List<Paragraph> paragraphs = new ArrayList<>();
        paragraphBreakMatcher.find();
        int paragraphStart = paragraphBreakMatcher.start();
        while(paragraphBreakMatcher.find()){
            int newStart = paragraphBreakMatcher.start();
            paragraphs.add(new Paragraph(paragraphStart, newStart));
            paragraphStart = newStart;
        }
        return paragraphs;
    }

    public Subdivision buildPageStructure(WikiPage page) {
        String text = page.getText();
        Matcher headingMatcher = headingPattern.matcher(text);
        Position rootPosition = new Position(0, 0);
        Subdivision root = new Subdivision(1, rootPosition, page.getTitle());
        if(headingMatcher.find()) {
            root.addChild(findChildren(headingMatcher, text, root));
        }
        return root;
    }

    private Subdivision findChildren(Matcher headingMatcher, String text, Subdivision root) {
        int currentOrder = root.getOrder() + 1;
        while (!headingMatcher.hitEnd()) {
            String title = text.substring(headingMatcher.start(),headingMatcher.end());
            if (!headingMatcher.find()) {
                break;
            }
            Position position = new Position(headingMatcher.start(), headingMatcher.end());
            int order = getSubdivisionOrder(title);
            Subdivision subdivision = new Subdivision(order, position, title);
            String sectionText = text.substring(0,headingMatcher.start());
            subdivision.setParagraphs(parseParagraphs(sectionText));
            if (order > currentOrder) {
                getLastChild(root).addChild(subdivision);
                Subdivision retChild = findChildren(headingMatcher, text, subdivision);
                if(retChild.getOrder() == currentOrder + 1){
                    getLastChild(root).addChild(retChild);
                } else if(retChild.getOrder() == currentOrder){
                    root.addChild(retChild);
                } else {
                    return retChild;
                }
            } else if (order < currentOrder){
                return subdivision;
            } else {
                root.addChild(subdivision);
            }
        }
        return root;
    }

    private int getSubdivisionOrder(String subdivisionText) {
        Pattern notEqual = Pattern.compile("[^=]+");
        Matcher textMatcher = notEqual.matcher(subdivisionText);
        if(!textMatcher.find()){
            throw new IllegalStateException("Wrong title format.");
        }
        String headingMarkup = subdivisionText.substring(0, textMatcher.start());
        return headingMarkup.length();
    }

    private Subdivision getLastChild(Subdivision subdivision) {
        List<Subdivision> children = subdivision.getChildren();
        return children.get(children.size() - 1);
    }

}
