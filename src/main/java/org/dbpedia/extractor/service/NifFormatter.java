package org.dbpedia.extractor.service;

import org.dbpedia.extractor.entity.*;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Class to format NIF entries
 */
@Component
public class NifFormatter {
    private static final String DBPEDIA_LINK = "http://dbpedia.org/resource";
    private static final String PERSISTENCE_ONTOLOGY_LINK = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core";
    private static final String WIKI_LINK = "http://en.wikipedia.org/wiki/";
    private static final String RDF_SYNTAX_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private static final String NON_NEGATIVE_INTEGER = "http://www.w3.org/2001/XMLSchema#nonNegativeInteger";
    private static final String ENG_LANG_URL = "http://lexvo.org/id/iso639-3/eng";
    private static final String BEGIN_INDEX = "beginIndex";
    private static final String END_INDEX = "endIndex";
    private static final String SOURCE_URL = "sourceUrl";
    private static final String REFERENCE_CONTEXT = "referenceContext";
    private static final String SUPER_STRING = "superString";
    private static final String HAS_PARAGRAPH = "hasParagraph";
    private static final String FIRST_PARAGRAPH = "firstParagraph";
    private static final String LAST_PARAGRAPH = "lastParagraph";
    private static final String IS_STRING = "isString";
    private static final String PRED_LANG = "predLang";
    private static final String NIF_CONTEXT = "context";

    private String currentDateString;

    public NifFormatter() {
        this.currentDateString = getCurrentDateString();
    }

    // Context block
    private static final String CONTEXT_NIF_TYPE = "Context";
    //TODO create a recursion class
    private String pageTitle;

    public String generateContextEntry(ParsedPage parsedPage) {
        Context context = parsedPage.getContext();
        String title = parsedPage.getTitle();
        int beginIndex = 0;
        int endIndex = context.getText().length();
        StringBuilder contextEntry = new StringBuilder();
        String dbpediaUrl = getDbpediaUrl(parsedPage.getTitle(), CONTEXT_NIF_TYPE);
        contextEntry.append(String.format("%s <%s> <%s#Context> .%s",
                dbpediaUrl, RDF_SYNTAX_TYPE, PERSISTENCE_ONTOLOGY_LINK, System.lineSeparator()));
        contextEntry.append(String.format("%s <%s> %s .%s",
                dbpediaUrl, getPersistenceOntologyUrl(BEGIN_INDEX), getIndexValue(beginIndex, BEGIN_INDEX), System.lineSeparator()));
        contextEntry.append(String.format("%s <%s> %s .%s",
                dbpediaUrl, getPersistenceOntologyUrl(END_INDEX), getIndexValue(endIndex, END_INDEX), System.lineSeparator()));
        contextEntry.append(String.format("%s <%s> <%s%s> .%s",
                dbpediaUrl, getPersistenceOntologyUrl(SOURCE_URL), WIKI_LINK, title, System.lineSeparator()));
        contextEntry.append(String.format("%s <%s> \"%s\" .%s",
                dbpediaUrl, getPersistenceOntologyUrl(IS_STRING), context.getText(), System.lineSeparator()));
        contextEntry.append(String.format("%s <%s> <%s> .%s",
                dbpediaUrl, getPersistenceOntologyUrl(PRED_LANG), ENG_LANG_URL, System.lineSeparator()));
        return contextEntry.toString();
    }

    public String generatePageStructureEntry(ParsedPage parsedPage) {
        StringBuilder pageStructureEntry = new StringBuilder();
        pageTitle = parsedPage.getTitle();
        pageStructureEntry.append(generateNodeEntry(parsedPage.getStructureRoot()));
        return pageStructureEntry.toString();
    }

    public String generateLinksEntry(ParsedPage parsedPage) {
        StringBuilder linksEntry = new StringBuilder();
        pageTitle = parsedPage.getTitle();
        linksEntry.append(generateLinkNodeEntry(parsedPage.getStructureRoot()));
        return linksEntry.toString();
    }

    private String generateNodeEntry(Subdivision node) {
        StringBuilder nodeEntry = new StringBuilder();
        String title = pageTitle;
        Position nodePosition = node.getPosition();
        int beginIndex = nodePosition.getStart();
        int endIndex = nodePosition.getEnd();
        String beginIndexString = Integer.toString(beginIndex);
        String endIndexString = Integer.toString(endIndex);
        String dbPediaSectionUrl = getDbpediaUrl(title, String.format("section_%s_%s", beginIndexString, endIndexString));
        String dbPediaContextUrl = getDbpediaUrl(title, NIF_CONTEXT);
        if(beginIndex != endIndex){
            nodeEntry.append(String.format("%s <%s> <%s#Section> .%s",
                    dbPediaSectionUrl, RDF_SYNTAX_TYPE, PERSISTENCE_ONTOLOGY_LINK, System.lineSeparator()));
            nodeEntry.append(String.format("%s <%s> <%s> .%s",
                    dbPediaSectionUrl, getPersistenceOntologyUrl(BEGIN_INDEX), getIndexValue(beginIndex, BEGIN_INDEX), System.lineSeparator()));
            nodeEntry.append(String.format("%s <%s> <%s> .%s",
                    dbPediaSectionUrl, getPersistenceOntologyUrl(END_INDEX), getIndexValue(endIndex, END_INDEX), System.lineSeparator()));

            nodeEntry.append(String.format("%s <%s> <%s> .%s",
                    dbPediaSectionUrl, getPersistenceOntologyUrl(REFERENCE_CONTEXT), dbPediaContextUrl, System.lineSeparator()));
            nodeEntry.append(String.format("%s <%s#hasSection> %s .%s",
                    dbPediaContextUrl, PERSISTENCE_ONTOLOGY_LINK, dbPediaSectionUrl, System.lineSeparator()));
        }
        int offset = beginIndex;
        int numberOfParagraphs = node.getParagraphs().size();
        for (int paragraphIndex = 0; paragraphIndex < numberOfParagraphs; paragraphIndex++) {
            Paragraph paragraph = node.getParagraphs().get(paragraphIndex);
            Position paragraphPosition = paragraph.getPosition();
            beginIndex = paragraphPosition.getStart();
            endIndex = paragraphPosition.getEnd();
            beginIndexString = Integer.toString(beginIndex);
            endIndexString = Integer.toString(endIndex);
            String urlParagraphSuffix = String.format("paragraph_%s_%s", beginIndexString, endIndexString);
            String dbPediaParagraphUrl = getDbpediaUrl(title, urlParagraphSuffix);
            //NIF Indexes
            nodeEntry.append(String.format("%s <%s> %s .%s",
                    dbPediaParagraphUrl, getPersistenceOntologyUrl(BEGIN_INDEX), getIndexValue(offset + beginIndex, BEGIN_INDEX), System.lineSeparator()));
            nodeEntry.append(String.format("%s <%s> %s .%s",
                    dbPediaParagraphUrl, getPersistenceOntologyUrl(END_INDEX), getIndexValue(offset + endIndex, END_INDEX), System.lineSeparator()));
            //Reference Context
            nodeEntry.append(String.format("%s <%s> %s .%s",
                    dbPediaParagraphUrl, getPersistenceOntologyUrl(REFERENCE_CONTEXT), dbPediaContextUrl, System.lineSeparator()));
            nodeEntry.append(String.format("%s <%s> %s .%s",
                    dbPediaParagraphUrl, getPersistenceOntologyUrl(SUPER_STRING), dbPediaSectionUrl, System.lineSeparator()));
            nodeEntry.append(String.format("%s <%s> %s .%s",
                    dbPediaSectionUrl, getPersistenceOntologyUrl(HAS_PARAGRAPH), dbPediaParagraphUrl, System.lineSeparator()));
            if (paragraphIndex == 0) {
                nodeEntry.append(String.format("%s <%s> %s .%s",
                        dbPediaSectionUrl, getPersistenceOntologyUrl(FIRST_PARAGRAPH), dbPediaParagraphUrl, System.lineSeparator()));
            } else if (paragraphIndex == numberOfParagraphs - 1) {
                nodeEntry.append(String.format("%s <%s> %s .%s",
                        dbPediaSectionUrl, getPersistenceOntologyUrl(LAST_PARAGRAPH), dbPediaParagraphUrl, System.lineSeparator()));
            }
        }
        //initiate recursion
        List<Subdivision> nodeChildren = node.getChildren();
        for (Subdivision child : nodeChildren) {
            nodeEntry.append(generateNodeEntry(child));
        }
        return nodeEntry.toString();
    }

    public String generateLinkNodeEntry(Subdivision node) {
        StringBuilder nodeEntry = new StringBuilder();
        String title = node.getTitle();
        Position nodePosition = node.getPosition();
        int beginIndex = nodePosition.getStart();
        int endIndex = nodePosition.getEnd();
        String beginIndexString = Integer.toString(beginIndex);
        String endIndexString = Integer.toString(endIndex);
        String dbPediaContextUrl = getDbpediaUrl(title, NIF_CONTEXT);
        int numberOfParagraphs = node.getParagraphs().size();

        for (int paragraphIndex = 0; paragraphIndex < numberOfParagraphs; paragraphIndex++) {
            Paragraph paragraph = node.getParagraphs().get(paragraphIndex);
            String urlParagraphSuffix = String.format("paragraph_%s_%s", paragraph.getPosition().getStart(), paragraph.getPosition().getEnd());
            String dbPediaParagraphUrl = getDbpediaUrl(title, urlParagraphSuffix);
            for (Link link : paragraph.getLinks()) {
                int linkBeginIndex = link.getPosition().getStart();
                int linkEndIndex = link.getPosition().getEnd();
                String linkBeginIndexString = Integer.toString(linkBeginIndex);
                String linkEndIndexString = Integer.toString(linkEndIndex);
                LinkType linkType = link.getLinkType();
                String dbPediaLinkUrl = getDbpediaUrl(title, String.format("%s_%s_%s",
                        linkType.getTypeLabel(), linkBeginIndexString, linkEndIndexString));
                // Link Type NIF
                nodeEntry.append(String.format("%s <%s> <%s#%s> .%s",
                        dbPediaLinkUrl, RDF_SYNTAX_TYPE, PERSISTENCE_ONTOLOGY_LINK,
                        linkType.getCapitalizedTypeLabel(), System.lineSeparator()));
                // Reference Context
                nodeEntry.append(String.format("%s <%s> %s .%s",
                        dbPediaLinkUrl, getPersistenceOntologyUrl(REFERENCE_CONTEXT), dbPediaContextUrl, System.lineSeparator()));
                //NIF Indexes
                nodeEntry.append(String.format("%s <%s> %s .%s",
                        dbPediaLinkUrl, getPersistenceOntologyUrl(BEGIN_INDEX), getIndexValue(linkBeginIndex, BEGIN_INDEX), System.lineSeparator()));
                nodeEntry.append(String.format("%s <%s> %s .%s",
                        dbPediaLinkUrl, getPersistenceOntologyUrl(END_INDEX), getIndexValue(linkEndIndex, END_INDEX), System.lineSeparator()));
                // Super String
                nodeEntry.append(String.format("%s <%s> %s .%s",
                        dbPediaLinkUrl, getPersistenceOntologyUrl(SUPER_STRING), dbPediaParagraphUrl, System.lineSeparator()));
                // anchorOf
                nodeEntry.append(String.format("%s <%s> \"%s\" .%s",
                        dbPediaLinkUrl, getPersistenceOntologyUrl("anchorOf"), link.getAnchorOf(), System.lineSeparator()));
            }
        }
        //initiate recursion
        List<Subdivision> nodeChildren = node.getChildren();
        for (Subdivision child : nodeChildren) {
            nodeEntry.append(generateLinkNodeEntry(child));
        }
        return nodeEntry.toString();
    }

    private String getDbpediaUrl(String title, String nifType) {
        return String.format("<%s/%s?dbpv=%s&nif=%s>", DBPEDIA_LINK, title, currentDateString, nifType);
    }

    private String getPersistenceOntologyUrl(String ontologyType) {
        return String.format("%s#%s", PERSISTENCE_ONTOLOGY_LINK, ontologyType);
    }

    private String getIndexValue(int index, String indexType) {
        return String.format("\"%d\"^^<%s#%s>", index, NON_NEGATIVE_INTEGER, indexType);
    }

    private String getCurrentDateString() {
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        return simpleDateFormat.format(currentDate);
    }
}
