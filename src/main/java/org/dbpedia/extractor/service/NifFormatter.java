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
        pageStructureEntry.append(generateNodeEntry(parsedPage.getStructureRoot()));
        return pageStructureEntry.toString();
    }

    public String generateLinksEntry(ParsedPage parsedPage) {
        StringBuilder linksEntry = new StringBuilder();
//        linksEntry.append("", );
        return linksEntry.toString();
    }

    private String generateNodeEntry(Subdivision node) {
        StringBuilder nodeEntry = new StringBuilder();
        String title = node.getTitle();
        Position nodePosition = node.getPosition();
        int beginIndex = nodePosition.getStart();
        int endIndex = nodePosition.getEnd();
        String beginIndexString = Integer.toString(beginIndex);
        String endIndexString = Integer.toString(endIndex);
        String dbPediaSectionUrl = getDbpediaUrl(title,String.format("section_%s_%s", beginIndexString, endIndexString));
        nodeEntry.append(String.format("%s <%s> <%s#Section> .%s",
                dbPediaSectionUrl, RDF_SYNTAX_TYPE, PERSISTENCE_ONTOLOGY_LINK, System.lineSeparator()));
        nodeEntry.append(String.format("%s <%s> <%s> .%s",
                dbPediaSectionUrl, getPersistenceOntologyUrl(BEGIN_INDEX), getIndexValue(beginIndex, BEGIN_INDEX), System.lineSeparator()));
        nodeEntry.append(String.format("%s <%s> <%s> .%s",
                dbPediaSectionUrl, getPersistenceOntologyUrl(END_INDEX), getIndexValue(endIndex, END_INDEX), System.lineSeparator()));

        String dbPediaContextUrl = getDbpediaUrl(title, NIF_CONTEXT);
        nodeEntry.append(String.format("%s <%s> <%s> .%s",
                dbPediaSectionUrl, getPersistenceOntologyUrl(REFERENCE_CONTEXT), dbPediaContextUrl, System.lineSeparator()));
        nodeEntry.append(String.format("%s <%s#hasSection> <%s> .%s",
                dbPediaContextUrl, PERSISTENCE_ONTOLOGY_LINK, dbPediaSectionUrl, System.lineSeparator()));
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
            nodeEntry.append(String.format("%s <%s> %s .%s",
                    dbPediaParagraphUrl, getPersistenceOntologyUrl(BEGIN_INDEX), getIndexValue(beginIndex, BEGIN_INDEX), System.lineSeparator()));
            nodeEntry.append(String.format("%s <%s> %s .%s",
                    dbPediaParagraphUrl, getPersistenceOntologyUrl(END_INDEX), getIndexValue(endIndex, END_INDEX), System.lineSeparator()));
            nodeEntry.append(String.format("%s <%s> %s .%s",
                    dbPediaParagraphUrl, getPersistenceOntologyUrl(REFERENCE_CONTEXT), dbPediaContextUrl, System.lineSeparator()));
            nodeEntry.append(String.format("%s <%s> %s .%s",
                    dbPediaParagraphUrl, getPersistenceOntologyUrl(SUPER_STRING), dbPediaSectionUrl, System.lineSeparator()));
            nodeEntry.append(String.format("%s <%s> %s .%s",
                    dbPediaSectionUrl, getPersistenceOntologyUrl(HAS_PARAGRAPH), dbPediaParagraphUrl, System.lineSeparator()));
            if(paragraphIndex == 0){
                nodeEntry.append(String.format("%s <%s> %s .%s",
                        dbPediaSectionUrl, getPersistenceOntologyUrl(FIRST_PARAGRAPH), dbPediaParagraphUrl, System.lineSeparator()));
            } else if(paragraphIndex == numberOfParagraphs - 1){
                nodeEntry.append(String.format("%s <%s> %s .%s",
                        dbPediaSectionUrl, getPersistenceOntologyUrl(LAST_PARAGRAPH), dbPediaParagraphUrl, System.lineSeparator()));
            }
        }
        //initiate recursion
        List<Subdivision> nodeChildren = node.getChildren();
        for(Subdivision child : nodeChildren){
            nodeEntry.append(generateNodeEntry(child));
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
        return String.format("\"%d\"^^<%s%s>", index, NON_NEGATIVE_INTEGER, indexType);
    }

    private String getCurrentDateString() {
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        return simpleDateFormat.format(currentDate);
    }
}
