package org.dbpedia.extractor.service;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.dbpedia.extractor.entity.*;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Stack;


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
    private static final String TA_IDENT_REF = "http://www.w3.org/2005/11/its/rdf#taIdentRef";
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

    //TODO create a recursion class
    private String pageTitle;

    public Model generateContextEntry(ParsedPage parsedPage) {
        Context context = parsedPage.getContext();
        String title = parsedPage.getTitle();
        int beginIndex = 0;
        int endIndex = context.getText().length();
        Model jenaModel =  ModelFactory.createDefaultModel();

        String dbpediaUrl = getDbpediaUrl(parsedPage.getTitle(), NIF_CONTEXT);
        Resource dbPediaResource = jenaModel.createResource(dbpediaUrl);
        Resource wordResource = jenaModel.createResource(PERSISTENCE_ONTOLOGY_LINK + "#" + LinkType.WORD.getCapitalizedTypeLabel());

        Property rdfSyntaxProperty = jenaModel.createProperty(RDF_SYNTAX_TYPE);
        dbPediaResource.addProperty(rdfSyntaxProperty, wordResource);

        Property beginIndexProperty = jenaModel.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+BEGIN_INDEX);
        dbPediaResource.addProperty(beginIndexProperty, jenaModel.createTypedLiteral(beginIndex, XSDDatatype.XSDnonNegativeInteger));

        Property endIndexProperty = jenaModel.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+END_INDEX);
        dbPediaResource.addProperty(endIndexProperty, jenaModel.createTypedLiteral(endIndex, XSDDatatype.XSDnonNegativeInteger));

        Property sourceUrlProperty = jenaModel.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+SOURCE_URL);
        dbPediaResource.addProperty(sourceUrlProperty, WIKI_LINK+title);

        Property contextStringProperty = jenaModel.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+IS_STRING);
        dbPediaResource.addProperty(contextStringProperty, context.getText());

        Property predLangProperty = jenaModel.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+PRED_LANG);
        dbPediaResource.addProperty(predLangProperty, ENG_LANG_URL);

        return jenaModel;
    }

    public Model generatePageStructureEntry(ParsedPage parsedPage) {
        Model pageStructureEntry = ModelFactory.createDefaultModel();
        pageTitle = parsedPage.getTitle();
        pageStructureEntry.add(generateNodeEntry(parsedPage.getStructureRoot()));
        return pageStructureEntry;
    }

    public String generateLinksEntry(ParsedPage parsedPage) {
        StringBuilder linksEntry = new StringBuilder();
        pageTitle = parsedPage.getTitle();
        linksEntry.append(generateLinkNodeEntry(parsedPage));
        return linksEntry.toString();
    }

    private Model generateNodeEntry(Subdivision node) {
        Model nodeEntry = ModelFactory.createDefaultModel();
        String title = pageTitle;
        Position nodePosition = node.getPosition();
        int beginIndex = nodePosition.getStart();
        int endIndex = nodePosition.getEnd();
        String beginIndexString = Integer.toString(beginIndex);
        String endIndexString = Integer.toString(endIndex);
        String dbPediaSectionUrl = getDbpediaUrl(title, String.format("section_%s_%s", beginIndexString, endIndexString));
        String dbPediaContextUrl = getDbpediaUrl(title, NIF_CONTEXT);

        if(beginIndex != endIndex){
            Resource dbPediaSectionResource = nodeEntry.createResource(dbPediaSectionUrl);

            Resource sectionResource = nodeEntry.createResource(PERSISTENCE_ONTOLOGY_LINK + "#Section");
            Property rdfSyntaxProperty = nodeEntry.createProperty(RDF_SYNTAX_TYPE);
            dbPediaSectionResource.addProperty(rdfSyntaxProperty, sectionResource);

            Property beginIndexProperty = nodeEntry.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+BEGIN_INDEX);
            dbPediaSectionResource
                    .addProperty(beginIndexProperty, nodeEntry.createTypedLiteral(beginIndex, XSDDatatype.XSDnonNegativeInteger));

            Property endIndexProperty = nodeEntry.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+END_INDEX);
            dbPediaSectionResource
                    .addProperty(endIndexProperty, nodeEntry.createTypedLiteral(endIndex, XSDDatatype.XSDnonNegativeInteger));

            Property referenceContextProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(REFERENCE_CONTEXT));
            dbPediaSectionResource
                    .addProperty(referenceContextProperty, dbPediaContextUrl);

            Property hasSectionProperty = nodeEntry.createProperty(getPersistenceOntologyUrl("hasSection"));
            dbPediaSectionResource
                    .addProperty(hasSectionProperty, dbPediaSectionUrl);

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
            Resource dbPediaParagraphResource = nodeEntry.createResource(dbPediaSectionUrl);

            Resource sectionResource = nodeEntry.createResource(PERSISTENCE_ONTOLOGY_LINK + "#Section");
            Property rdfSyntaxProperty = nodeEntry.createProperty(RDF_SYNTAX_TYPE);
            dbPediaParagraphResource.addProperty(rdfSyntaxProperty, sectionResource);

            //NIF Indexes
            Property beginIndexProperty = nodeEntry.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+BEGIN_INDEX);
            dbPediaParagraphResource.addProperty(beginIndexProperty,
                    nodeEntry.createTypedLiteral(offset + beginIndex, XSDDatatype.XSDnonNegativeInteger));

            Property endIndexProperty = nodeEntry.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+END_INDEX);
            dbPediaParagraphResource.addProperty(endIndexProperty,
                    nodeEntry.createTypedLiteral(offset + endIndex, XSDDatatype.XSDnonNegativeInteger));

            //Reference Context

            Property resourceContextProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(REFERENCE_CONTEXT));
            dbPediaParagraphResource.addProperty(resourceContextProperty,dbPediaContextUrl);

            Property superStringProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(SUPER_STRING));
            dbPediaParagraphResource.addProperty(superStringProperty,  dbPediaSectionUrl);

            Property hasParagraphProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(HAS_PARAGRAPH));
            dbPediaParagraphResource.addProperty(hasParagraphProperty,  dbPediaParagraphUrl);

            if (paragraphIndex == 0) {
                Property firstParagraphProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(FIRST_PARAGRAPH));
                dbPediaParagraphResource.addProperty(firstParagraphProperty,  dbPediaParagraphUrl);
            } else if (paragraphIndex == numberOfParagraphs - 1) {
                Property lastParagraphProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(LAST_PARAGRAPH));
                dbPediaParagraphResource.addProperty(lastParagraphProperty,  dbPediaParagraphUrl);
            }
        }
        //initiate recursion
        List<Subdivision> nodeChildren = node.getChildren();
        for (Subdivision child : nodeChildren) {
            nodeEntry.add(generateNodeEntry(child));
        }
        return nodeEntry;
    }

    public String generateLinkNodeEntry(ParsedPage parsedPage) {
        StringBuilder nodeEntry = new StringBuilder();
        String articleTitle = parsedPage.getTitle();
        Stack<Subdivision> nodeStack = new Stack<>();
        nodeStack.push(parsedPage.getStructureRoot());
        while(!nodeStack.empty()) {
            Subdivision node = nodeStack.pop();
            String dbPediaContextUrl = getDbpediaUrl(articleTitle, NIF_CONTEXT);
            int numberOfParagraphs = node.getParagraphs().size();

            for (int paragraphIndex = 0; paragraphIndex < numberOfParagraphs; paragraphIndex++) {
                Paragraph paragraph = node.getParagraphs().get(paragraphIndex);
                String urlParagraphSuffix = String.format("paragraph_%s_%s", paragraph.getPosition().getStart(), paragraph.getPosition().getEnd());
                String dbPediaParagraphUrl = getDbpediaUrl(articleTitle, urlParagraphSuffix);
                for (Link link : paragraph.getLinks()) {
                    int linkBeginIndex = link.getPosition().getStart();
                    int linkEndIndex = link.getPosition().getEnd();
                    String linkBeginIndexString = Integer.toString(linkBeginIndex);
                    String linkEndIndexString = Integer.toString(linkEndIndex);
                    LinkType linkType = link.getLinkType();
                    String dbPediaLinkUrl = getDbpediaUrl(articleTitle, String.format("%s_%s_%s",
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
                            dbPediaLinkUrl, getPersistenceOntologyUrl(BEGIN_INDEX), getIndexValue(linkBeginIndex), System.lineSeparator()));
                    nodeEntry.append(String.format("%s <%s> %s .%s",
                            dbPediaLinkUrl, getPersistenceOntologyUrl(END_INDEX), getIndexValue(linkEndIndex), System.lineSeparator()));
                    // Super String
                    nodeEntry.append(String.format("%s <%s> %s .%s",
                            dbPediaLinkUrl, getPersistenceOntologyUrl(SUPER_STRING), dbPediaParagraphUrl, System.lineSeparator()));
                    // taIdentRef
                    String dbPediaIdentRef = String.format("<%s/%s>", DBPEDIA_LINK, link.getTaIdentRef());
                    nodeEntry.append(String.format("%s <%s> %s .%s",
                            dbPediaLinkUrl, TA_IDENT_REF, dbPediaIdentRef, System.lineSeparator()));
                    // anchorOf
                    nodeEntry.append(String.format("%s <%s> \"%s\" .%s",
                            dbPediaLinkUrl, getPersistenceOntologyUrl("anchorOf"), link.getAnchorOf(), System.lineSeparator()));
                }
            }

            List<Subdivision> children = node.getChildren();
            Collections.reverse(children);
            for (Subdivision child : children) {
                nodeStack.push(child);
            }
            Collections.reverse(children);
        }
        return nodeEntry.toString();
    }

    private String getDbpediaUrl(String title, String nifType) {
        return String.format("%s/%s?dbpv=%s&nif=%s", DBPEDIA_LINK, title, currentDateString, nifType);
    }

    private String getPersistenceOntologyUrl(String ontologyType) {
        return String.format("%s#%s", PERSISTENCE_ONTOLOGY_LINK, ontologyType);
    }

    private String getIndexValue(int index) {
        return String.format("\"%d\"^^<%s>", index, NON_NEGATIVE_INTEGER);
    }

    private String getCurrentDateString() {
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        return simpleDateFormat.format(currentDate);
    }
}
