package org.dbpedia.extractor.service;

import org.dbpedia.extractor.entity.Context;
import org.dbpedia.extractor.entity.ParsedPage;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    private static final String IS_STRING = "isString";
    private static final String PRED_LANG = "predLang";

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
