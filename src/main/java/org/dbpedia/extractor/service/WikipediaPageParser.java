package org.dbpedia.extractor.service;

import org.dbpedia.extractor.entity.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser to split WikiPage into ParsedPage: paragraphs,links, page structure
 */
@Service
public class WikipediaPageParser {

    private static Pattern paragraphBreakPattern = Pattern.compile("\\r?\\n\\n");
    private static Pattern headingPattern = Pattern.compile("=+.+=+\\n");
    private static Map<String, String> matchingBraces;
    private static String LINK_START_PATTERN = "[[";
    private static String LINK_END_PATTERN = "]]";

    public WikipediaPageParser() {
        //initialize braces map
        matchingBraces = new HashMap<>();
        matchingBraces.put("{{", "}}");
        matchingBraces.put("<!--", "-->");
        matchingBraces.put("{|", "|}");
    }

    public ParsedPage parsePage(WikiPage page) {
        ParsedPage parsedPage = new ParsedPage();
        String text = page.getText();
        text = removeInfobox(text);
        text = removeFiles(text);
        text = removeFooters(text);
        page.setText(text);
        parsedPage.setWikiPage(page);
        parsedPage.setStructureRoot(buildPageStructure(page));
        Context context = new Context(createContext(parsedPage.getStructureRoot()));
        parsedPage.setContext(context);
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
            String paragraphText = text.substring(paragraphStart, newStart);
            Paragraph paragraph = parseParagraph(paragraphText);
            paragraph.setPosition(new Position(paragraphStart, paragraphStart + paragraph.getContext().length()));
            paragraphs.add(paragraph);
            paragraphStart = newStart;
        }
        return paragraphs;
    }

    /**
     * Build page tree, with sections and subsections as nodes.
     *
     * @param page
     * @return page tree root
     */
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

    private Link parseLink(String linkText) {
        LinkType linkType = determineLinkType(linkText);
        String linkAnchor = getLinkAnchor(linkText);
        Link link = new Link(linkType, linkAnchor);
        return link;
    }

    private LinkType determineLinkType(String linkText) {
        LinkType linkType;
        long wordCount = linkText.chars().filter(ch -> ch == ' ').count();
        if (wordCount == 0) {
            linkType = LinkType.WORD;
        } else {
            linkType = LinkType.PHRASE;
        }
        return linkType;
    }

    private String getLinkAnchor(String link) {
        String result = link;
        String[] linkArray = result.split("\\|");
        result = String.format("\"%s\"", linkArray[linkArray.length - 1]);
        return result;
    }

    private String removeInfobox(String text) {
        String infoboxStartPattern = "{{Infobox";
        String linkStart = LINK_START_PATTERN;
        String linkEnd = LINK_END_PATTERN;
        int i = 0;
        while (text.contains(infoboxStartPattern)) {
            int infoboxStart = text.indexOf(infoboxStartPattern);
            int parenthesesCounter = 1;
            i = infoboxStart;
            while (parenthesesCounter > 0) {
                i++;
                String testSubStr = text.substring(i, i + 2);
                if (testSubStr.equals(linkStart)) {
                    parenthesesCounter++;
                } else if (testSubStr.equals(linkEnd)) {
                    parenthesesCounter--;
                }
            }
            text = text.substring(0, infoboxStart) + text.substring(i + 3);
        }
        return text;
    }

    private String removeFiles(String text) {
        String fileStartPattern = "[[File";
        String figureStart = "[[";
        String figureEnd = "]]";
        int i = 0;
        while (text.contains(fileStartPattern)) {
            int fileStart = text.indexOf(fileStartPattern);
            int parenthesesCounter = 1;
            i = fileStart;
            while (parenthesesCounter > 0) {
                i++;
                String testSubStr = text.substring(i, i + 2);
                if (testSubStr.equals(figureStart)) {
                    parenthesesCounter++;
                } else if (testSubStr.equals(figureEnd)) {
                    parenthesesCounter--;
                }
            }
            text = text.substring(0, fileStart) + text.substring(i + 3);
        }
        return text;
    }

    private String removeFooters(String text) {
        String footerStart = "== See also ==";
        if (text.contains(footerStart)) {
            int footerStartIndex = text.indexOf(footerStart);
            text = text.substring(0, footerStartIndex);
        }
        return text;
    }

    /**
     * Find end of an xml component in braces
     *
     * @param text        XML text
     * @param bracesStart brace start
     * @param bracesPos   brace position
     * @return index of the brace end
     */
    private int findMatchingBracesIndex(String text, String bracesStart, int bracesPos) {
        String bracesEnd = matchingBraces.get(bracesStart);
        Integer bracesEndLen = bracesEnd.length();
        int parenthesesCounter = 1;
        int i = bracesPos;
        while (parenthesesCounter > 0) {
            i++;
            String testSubStr = text.substring(i, i + bracesEndLen);
            if (testSubStr.equals(bracesStart)) {
                parenthesesCounter++;
            } else if (testSubStr.equals(bracesEnd)) {
                parenthesesCounter--;
            }
        }
        return i;
    }

    private Paragraph parseParagraph(String text) {
        Set<String> xmlTagSet = matchingBraces.keySet();
        List<Link> links = new ArrayList<>();
        StringBuilder cleanedText = new StringBuilder();

        for (String xmlTag : xmlTagSet) {
            int index = text.indexOf(xmlTag);
            int previousIndex = 0;
            while (index >= 0) {
                cleanedText.append(text, previousIndex, index);
                int tagEndIndex = findMatchingBracesIndex(text, xmlTag, index);
                int tagEndLength = matchingBraces.get(xmlTag).length();

                previousIndex = tagEndIndex + tagEndLength;
                index = text.indexOf(xmlTag, index + tagEndLength);
            }
            text = cleanedText.toString();
            cleanedText = new StringBuilder();
//            String cleanedXmlString = text.substring(index + xmlTag.length(), tagEndIndex);
//            if (xmlTag.equals(LINK_START_PATTERN)) {
//                // add link
//                Link link = parseLink(cleanedXmlString);
//                cleanedXmlString = link.getAnchorOf();
////                    links.add(link);
//                cleanedText.append(cleanedXmlString);
//            }
        }
        Paragraph paragraph = new Paragraph();
        for (Link link : links) {
            link.setSuperString(paragraph);
        }
        paragraph.setLinks(links);
        paragraph.setContext(text);
        return paragraph;
    }

    /**
     * Combine sections into context
     *
     * @param root
     * @return
     */
    private String createContext(Subdivision root) {
        StringBuilder result = new StringBuilder();
        List<Paragraph> paragraphs = root.getParagraphs();
        result.append(root.getTitle()).append("\n");
        for (Paragraph paragraph : paragraphs) {
            result.append(paragraph.getContext()).append("\n");
        }
        for (Subdivision child : root.getChildren()) {
            result.append(createContext(child));
        }
        return result.toString();
    }
}
