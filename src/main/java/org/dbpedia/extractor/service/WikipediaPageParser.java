package org.dbpedia.extractor.service;

import org.apache.commons.text.StringEscapeUtils;
import org.dbpedia.extractor.entity.*;
import org.dbpedia.extractor.service.remover.WikiTagsRemover;
import org.dbpedia.extractor.service.remover.language.LanguageIdentifierBean;
import org.dbpedia.extractor.service.transformer.ContextLanguageTransformer;
import org.dbpedia.extractor.service.transformer.XmlTransformer;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static List<String> tagsToRemove;
    private static String LINK_START_PATTERN = "[[";
    private static String LINK_END_PATTERN = "]]";

    private final XmlTransformer contextLanguageTransformer;
    private final BracesMatcher bracesMatcher = new BracesMatcher();

    @Autowired
    private WikiTagsRemover wikiTagsRemover;

    @Autowired
    private LanguageIdentifierBean languageIdentifierBean;

    public WikipediaPageParser(ContextLanguageTransformer contextLanguageTransformer) {

        //initalize tags to remove
        tagsToRemove = new ArrayList<>();
        tagsToRemove.add("{{");
        tagsToRemove.add("<!--");
        tagsToRemove.add("<ref>");
        tagsToRemove.add("{|");
        this.contextLanguageTransformer = contextLanguageTransformer;
    }

    public ParsedPage parsePage(WikiPage page) {
        ParsedPage parsedPage = new ParsedPage();
        String text = page.getText();
        text = removeInfobox(text);
        text = removeInfoObjects(text, "[[File");
        text = removeInfoObjects(text, "[[Image");
        text = removeInfoObjects(text, "[[Datei");
        text = removeApostrophes(text);
        wikiTagsRemover.setLanguageFooterRemover(languageIdentifierBean.getLanguage());
        text = wikiTagsRemover.fixUnitConversion(text);
        text = wikiTagsRemover.removeEmphasis(text);
        text = wikiTagsRemover.removeGallery(text);
        text = wikiTagsRemover.removeHtmlComments(text);
        text = wikiTagsRemover.removeIndentation(text);
        text = wikiTagsRemover.removeMath(text);
        text = wikiTagsRemover.removeNoToc(text);
        text = wikiTagsRemover.removeParentheticals(text);
        text = wikiTagsRemover.removeFooter(text);
        text = wikiTagsRemover.removeFooter(text);
        // some HTML entities are doubly encoded.
        text = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(text));
        text = wikiTagsRemover.removeHtmlTags(text);
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
        Subdivision root = new Subdivision(1, rootPosition, "");
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

    private String pruneTitle(String title) {
        int orderOfTitle = 0;
        while (title.charAt(orderOfTitle) == '=') {
            orderOfTitle++;
        }
        title = title.substring(orderOfTitle, title.length() - orderOfTitle - 1);
        title = title.trim();
        return title;
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
        result = String.format("%s", linkArray[linkArray.length - 1]);
        return result;
    }

    private String removeInfobox(String text) {
        String infoboxStartPattern = "{{Infobox";
        String linkStart = "{{";
        String linkEnd = "}}";
        int i = -1;
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

    /**
     * Remove file xml objects from text
     * @param text input text
     * @return text with removed files
     */
    private String removeInfoObjects(String text, String startPattern) {
        String figureStart = "[[";
        String figureEnd = "]]";
        int i = 0;
        while (text.contains(startPattern)) {
            int fileStart = text.indexOf(startPattern);
            int parenthesesCounter = 1;
            i = fileStart;
            while (parenthesesCounter > 0) {
                i++;
                String testSubStr = text.substring(i, i + figureEnd.length());
                if (testSubStr.equals(figureStart)) {
                    parenthesesCounter++;
                } else if (testSubStr.equals(figureEnd)) {
                    parenthesesCounter--;
                }
            }
            text = text.substring(0, fileStart) + text.substring(i + figureEnd.length() + 1);
        }
        return text;
    }

    private String removeApostrophes(String text){
        text = text.replaceAll("'{2,}","");
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

    private Paragraph parseParagraph(String text) {
        List<Link> links = new ArrayList<>();
        //preprocess text for special xml entries
        text = contextLanguageTransformer.transformText(text);
        text = mirrorParentheses(text);

        StringBuilder cleanedText = new StringBuilder();
        for (String xmlTag : tagsToRemove) {
            int index = text.indexOf(xmlTag);
            if(index == -1){ // skip if there is no xml instances
                continue;
            }
            int previousIndex = 0;
            while (index >= 0) {
                cleanedText.append(text, previousIndex, index);
                int tagEndIndex = bracesMatcher.findMatchingBracesIndex(text, xmlTag, index);
                int tagEndLength = BracesMatcher.matchingBraces.get(xmlTag).length();
                previousIndex = tagEndIndex + tagEndLength;
                index = text.indexOf(xmlTag, tagEndIndex + 1);
            }
            cleanedText.append(text, previousIndex, text.length());
            text = cleanedText.toString();
            cleanedText = new StringBuilder();
        }
        text = trimParagraphText(text);
        text = removeLineBreaks(text);

        //parse links
        int index = text.indexOf(LINK_START_PATTERN);
        int previousIndex = 0;
        int tagEndIndex = 0;
        while (index >= 0) {
            cleanedText.append(text, previousIndex, index);
            tagEndIndex = bracesMatcher.findMatchingBracesIndex(text, LINK_START_PATTERN, index);
            int tagEndLength = BracesMatcher.matchingBraces.get(LINK_START_PATTERN).length();
            previousIndex = tagEndIndex + tagEndLength;
            String cleanedXmlString = text.substring(index + LINK_START_PATTERN.length(), tagEndIndex);
            // add link
            Link link = parseLink(cleanedXmlString);
            cleanedXmlString = link.getAnchorOf();
            Position linkPosition = new Position(cleanedText.length(),
                    cleanedText.length() + cleanedXmlString.length());
            link.setPosition(linkPosition);
            links.add(link);
            cleanedText.append(cleanedXmlString);
            index = text.indexOf(LINK_START_PATTERN, tagEndIndex + 1);
        }
        if(previousIndex < text.length()){
            cleanedText.append(text.substring(previousIndex));
        }
        text = cleanedText.toString();

        Paragraph paragraph = new Paragraph();
        for (Link link : links) {
            link.setSuperString(paragraph);
        }
        paragraph.setLinks(links);
        paragraph.setContext(text);
        return paragraph;
    }

    /**
     * Replace all line breaks with text representations
     * @param text
     * @return
     */
    private String removeLineBreaks(String text){
        text = text.replaceAll("\n","\\\\n");
        return text;
    }

    private String trimParagraphText(String text){
        return text.trim();
    }

    private String mirrorParentheses(String text){
        text = text.replaceAll("\"","\\\\\"");
        return text;
    }

    /**
     * Combine sections into context, set link and paragraph positions
     *
     * @param root
     * @return
     */
    private String createContext(Subdivision root) {
        StringBuilder result = new StringBuilder();
        Stack<Subdivision> nodeStack = new Stack<>();
        nodeStack.push(root);
        while(!nodeStack.empty()){
            Subdivision node = nodeStack.pop();
            int sectionStart = result.length();
            List<Paragraph> paragraphs = node.getParagraphs();
            if(!node.getTitle().isEmpty()){
                result.append(node.getTitle()).append("\\n");
            }
            for (Paragraph paragraph : paragraphs) {
                int paragraphStart = result.length();
                paragraph.setPosition(new Position(paragraphStart,paragraphStart + paragraph.getContext().length()));
                List<Link> links = paragraph.getLinks();
                for(Link link : links){
                    link.getPosition().addOffset(paragraphStart);
                }
                result.append(paragraph.getContext());
            }
            if(!paragraphs.isEmpty()){
                result.append("\\n\\n");
            }
            node.setPosition(new Position(sectionStart, result.length()));
            List<Subdivision> children = node.getChildren();
            Collections.reverse(children);
            for (Subdivision child : children) {
                nodeStack.push(child);
            }
            Collections.reverse(children);
        }
        return result.toString();
    }
}
