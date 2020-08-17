package org.dbpedia.extractor.service;

import org.dbpedia.extractor.entity.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser to split WikiPage into ParsedPage: paragraphs,links, page structure
 */
@Service
public class WikipediaPageParser {

    private static Pattern paragraphBreakPattern = Pattern.compile("\\r?\\n\\n");
    private static Pattern headingPattern = Pattern.compile("=+.+=+\\n");

    public ParsedPage parsePage(WikiPage page) {
        ParsedPage parsedPage = new ParsedPage();
        String text = page.getText();
        text = removeInfobox(text);
        Context context = new Context(text);
        parsedPage.setWikiPage(page);
        parsedPage.setContext(context);
        parsedPage.setStructureRoot(buildPageStructure(page));
        return parsedPage;
    }

    /**
     * @param text wiki article
     * @return list of paragraphs
     */
    private List<Paragraph> parseParagraphs(String text) {
        Matcher paragraphBreakMatcher = paragraphBreakPattern.matcher(text);
        List<Paragraph> paragraphs = new ArrayList<>();
        int paragraphStart = 0;
        while (paragraphBreakMatcher.find()) {
            int newStart = paragraphBreakMatcher.start();
            Paragraph paragraph = new Paragraph(paragraphStart, newStart);
            paragraph.setLinks(parseLinks(text, paragraph));
            paragraphs.add(paragraph);
            paragraphStart = newStart;
        }
        return paragraphs;
    }

    public Subdivision buildPageStructure(WikiPage page) {
        String text = page.getText();
        Matcher headingMatcher = headingPattern.matcher(text);
        Position rootPosition;
        List<Position> headingPositions = new ArrayList<>();
        while (headingMatcher.find()) {
            headingPositions.add(new Position(headingMatcher.start(), headingMatcher.end()));
        }
        if (!headingPositions.isEmpty()) {
            rootPosition = new Position(0, headingPositions.get(0).getStart());
        } else {
            rootPosition = new Position(0, text.length() - 1);
        }
        Subdivision root = new Subdivision(1, rootPosition, page.getTitle());
        root.setParagraphs(parseParagraphs(text.substring(rootPosition.getStart(), rootPosition.getEnd())));
        Stack<Subdivision> subdivisionStack = new Stack<>();
        subdivisionStack.push(root);
        for (int i = 0; i < headingPositions.size(); i++) {
            Position headPos = headingPositions.get(i);
            String title = text.substring(headPos.getStart(), headPos.getEnd());
            int order = getSubdivisionOrder(title);
            title = pruneTitle(title);
            int sectionEnd;
            if (i < headingPositions.size() - 1) {
                sectionEnd = headingPositions.get(i + 1).getStart();
            } else { // special case for last paragraph
                sectionEnd = text.length() - 1;
            }
            String sectionText = text.substring(headPos.getEnd(), sectionEnd);
            Position subdivPos = new Position(headPos.getStart(), sectionEnd);
            Subdivision subdivision = new Subdivision(order, subdivPos, title);
            subdivision.setParagraphs(parseParagraphs(sectionText));
            Subdivision prevDiv = subdivisionStack.peek();
            while (order < prevDiv.getOrder()) {
                prevDiv = subdivisionStack.pop();
            }
            if (order > prevDiv.getOrder()) {
                subdivision.setParent(prevDiv);
                prevDiv.addChild(subdivision);
            } else {
                Subdivision parent = prevDiv.getParent();
                subdivision.setParent(parent);
                parent.addChild(subdivision);
            }
            subdivisionStack.push(subdivision);
        }
        return root;
    }

    private int getSubdivisionOrder(String subdivisionText) {
        Pattern notEqual = Pattern.compile("[^=]+");
        Matcher textMatcher = notEqual.matcher(subdivisionText);
        if (!textMatcher.find()) {
            throw new IllegalStateException("Wrong title format.");
        }
        String headingMarkup = subdivisionText.substring(0, textMatcher.start());
        return headingMarkup.length();
    }

    private Subdivision getLastChild(Subdivision subdivision) {
        List<Subdivision> children = subdivision.getChildren();
        return children.get(children.size() - 1);
    }

    private String pruneTitle(String title) {
        int orderOfTitle = 0;
        while (title.charAt(orderOfTitle) == '=') {
            orderOfTitle++;
        }
        return title.substring(orderOfTitle, title.length() - orderOfTitle - 1);
    }

    //// LINKS PARSING /////

    private List<Link> parseLinks(String text, Paragraph paragraph) {
        List<Link> links = new ArrayList<>();
        String linkOp = "[[";
        String linkClosing = "]]";
        int length = text.length();
        Stack<Integer> openingsStack = new Stack<>();
        for (int i = 0; i < length - 1; i++) {
            String testSubStr = text.substring(i, i + 2);
            if (testSubStr.equals(linkOp)) {
                openingsStack.push(i);
                i++;
            } else if (testSubStr.equals(linkClosing)) {
                Integer linkOpPosition = openingsStack.pop();
                Position linkPosition = new Position(linkOpPosition, i + 1);
                String linkText = text.substring(linkPosition.getStart(), linkPosition.getEnd());
                LinkType linkType = determineLinkType(linkText);
                String linkAnchor = getLinkAnchor(linkText);
                Link link = new Link(linkPosition, linkType, linkAnchor);
                link.setSuperString(paragraph);
                links.add(link);
                i++;
            }
        }
        return links;
    }

    private LinkType determineLinkType(String linkText) {
        LinkType linkType;
        long wordCount = linkText.chars().filter(ch -> ch == ' ').count();
        if (wordCount == 1) {
            linkType = LinkType.WORD;
        } else {
            linkType = LinkType.PHRASE;
        }
        return linkType;
    }

    private String getLinkAnchor(String link) {
        String result = link.substring(2, link.length() - 1); // remove parentheses
        String[] linkArray = result.split("\\|");
        result = String.format("\"%s\"", linkArray[0]);
        return result;
    }

    private String removeInfobox(String text) {
        String infoboxStartPattern = "{{Infobox";
        String figureStart = "{{";
        String figureEnd = "}}";
        if (!text.contains(infoboxStartPattern)) {
            //return unmodified text
            return text;
        }
        int infoboxStart = text.indexOf(infoboxStartPattern);
        int parenthesesCounter = 1;
        int i = infoboxStart;
        while (parenthesesCounter > 0) {
            i++;
            String testSubStr = text.substring(i, i + 2);
            if (testSubStr.equals(figureStart)) {
                parenthesesCounter++;
            } else if (testSubStr.equals(figureEnd)) {
                parenthesesCounter--;
            }
        }
        return text.substring(i + 3);
    }
}
