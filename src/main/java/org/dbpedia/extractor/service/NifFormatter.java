package org.dbpedia.extractor.service;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.dbpedia.extractor.entity.*;
import org.dbpedia.extractor.service.remover.language.LanguageIdentifierBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private static final String LANG_URL = "http://lexvo.org/id/iso639-3/";
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
    private static final String HAS_SECTION = "hasSection";

    @Autowired
    private LanguageIdentifierBean languageIdentifierBean;

    private String currentDateString;

    public NifFormatter() {
        this.currentDateString = getCurrentDateString();
    }

    //TODO create a recursion class

    private String pageTitle;
    public Model generateContextEntry(ParsedPage parsedPage) {
        Context context = parsedPage.getContext();
        String title = encodeSpaces(parsedPage.getTitle());
        int beginIndex = 0;
        int endIndex = context.getText().length();
        Model jenaModel =  ModelFactory.createDefaultModel();

        String dbpediaUrl = getDbpediaUrl(title, NIF_CONTEXT);
        Resource dbPediaResource = jenaModel.createResource(dbpediaUrl);
        Resource contextResource = jenaModel.createResource(PERSISTENCE_ONTOLOGY_LINK + "#" + LinkType.CONTEXT.getCapitalizedTypeLabel());

        // Context NIF type
        Property rdfSyntaxProperty = jenaModel.createProperty(RDF_SYNTAX_TYPE);
        dbPediaResource.addProperty(rdfSyntaxProperty, contextResource);
        // Context beginIndex
        Property beginIndexProperty = jenaModel.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+BEGIN_INDEX);
        dbPediaResource.addProperty(beginIndexProperty, jenaModel.createTypedLiteral(beginIndex, XSDDatatype.XSDnonNegativeInteger));
        //Context endIndex
        Property endIndexProperty = jenaModel.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+END_INDEX);
        dbPediaResource.addProperty(endIndexProperty, jenaModel.createTypedLiteral(endIndex, XSDDatatype.XSDnonNegativeInteger));
        // Context sourceUrl
        Property sourceUrlProperty = jenaModel.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+SOURCE_URL);
        dbPediaResource.addProperty(sourceUrlProperty, WIKI_LINK+title);
        // Context isString string corpus
        Property contextStringProperty = jenaModel.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+IS_STRING);
        dbPediaResource.addProperty(contextStringProperty, context.getText());
        // Context language
        Property predLangProperty = jenaModel.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+PRED_LANG);
        dbPediaResource.addProperty(predLangProperty, LANG_URL+languageIdentifierBean.getLanguage().getIsoLangCode());

        return jenaModel;
    }

    public void setLanguageIdentifierBean(LanguageIdentifierBean languageIdentifierBean) {
        this.languageIdentifierBean = languageIdentifierBean;
    }

    public Model generatePageStructureEntry(ParsedPage parsedPage) {
        Model pageStructureEntry = ModelFactory.createDefaultModel();
        pageTitle = encodeSpaces(parsedPage.getTitle());


        // add has Section property for the context
        String dbPediaContextUrl = getDbpediaUrl(pageTitle, NIF_CONTEXT);
        Resource dbPediaContextResource = pageStructureEntry.createResource(dbPediaContextUrl);
        int beginIndex = 0;
        int endIndex = parsedPage.getContext().getText().length();
        String beginIndexString = Integer.toString(beginIndex);
        String endIndexString = Integer.toString(endIndex);
        String dbPediaSectionUrl = getDbpediaUrl(pageTitle, String.format("section_%s_%s", beginIndexString, endIndexString));
        Property contextHasSectionProperty = pageStructureEntry.createProperty(getPersistenceOntologyUrl(HAS_SECTION));
        dbPediaContextResource
                .addProperty(contextHasSectionProperty, dbPediaSectionUrl);

        pageStructureEntry.add(generateNodeEntry(parsedPage.getStructureRoot()));
        return pageStructureEntry;
    }

    public Model generateLinksEntry(ParsedPage parsedPage) {
        Model linksEntry = ModelFactory.createDefaultModel();
        pageTitle = encodeSpaces(parsedPage.getTitle());
        linksEntry.add(generateLinkNodeEntry(parsedPage));
        return linksEntry;
    }

    private Model generateNodeEntry(Subdivision node) {
        Model nodeEntry = ModelFactory.createDefaultModel();
        String title = encodeSpaces(pageTitle);
        Position nodePosition = node.getPosition();
        int beginIndex = nodePosition.getStart();
        int endIndex = nodePosition.getEnd();
        String beginIndexString = Integer.toString(beginIndex);
        String endIndexString = Integer.toString(endIndex);
        String dbPediaSectionUrl = getDbpediaUrl(title, String.format("section_%s_%s", beginIndexString, endIndexString));
        String dbPediaContextUrl = getDbpediaUrl(title, NIF_CONTEXT);


        Resource dbPediaSectionResource = nodeEntry.createResource(dbPediaSectionUrl);

        if(beginIndex != endIndex){

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

            for(Subdivision subSection: node.getChildren()){
                Position subSectionPosition = subSection.getPosition();
                int subBeginIndex = subSectionPosition.getStart();
                int subEndIndex = subSectionPosition.getEnd();
                String subBeginIndexString = Integer.toString(subBeginIndex);
                String subEndIndexString = Integer.toString(subEndIndex);
                String dbPediaSubSectionUrl
                        = getDbpediaUrl(title, String.format("section_%s_%s", subBeginIndexString, subEndIndexString));
                Property hasSectionProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(HAS_SECTION));
                dbPediaSectionResource
                        .addProperty(hasSectionProperty, dbPediaSubSectionUrl);
            }

        }
        int offset = beginIndex;
        int numberOfParagraphs = node.getParagraphs().size();
        boolean firstParagraphDone = false;
        for (int paragraphIndex = 0; paragraphIndex < numberOfParagraphs; paragraphIndex++) {
            Paragraph paragraph = node.getParagraphs().get(paragraphIndex);
            Position paragraphPosition = paragraph.getPosition();
            beginIndex = paragraphPosition.getStart();
            endIndex = paragraphPosition.getEnd();
            // skip paragraph if empty
            if(beginIndex == endIndex)
                continue;
            beginIndexString = Integer.toString(beginIndex);
            endIndexString = Integer.toString(endIndex);
            String urlParagraphSuffix = String.format("paragraph_%s_%s", beginIndexString, endIndexString);
            String dbPediaParagraphUrl = getDbpediaUrl(title, urlParagraphSuffix);
            Resource dbPediaParagraphResource = nodeEntry.createResource(dbPediaParagraphUrl);

            Resource paragraphResource = nodeEntry.createResource(PERSISTENCE_ONTOLOGY_LINK + "#Paragraph");
            Property rdfSyntaxProperty = nodeEntry.createProperty(RDF_SYNTAX_TYPE);
            dbPediaParagraphResource.addProperty(rdfSyntaxProperty, paragraphResource);

            //NIF Indexes
            Property beginIndexProperty = nodeEntry.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+BEGIN_INDEX);
            dbPediaParagraphResource.addProperty(beginIndexProperty,
                    nodeEntry.createTypedLiteral(beginIndex, XSDDatatype.XSDnonNegativeInteger));

            Property endIndexProperty = nodeEntry.createProperty(PERSISTENCE_ONTOLOGY_LINK, "#"+END_INDEX);
            dbPediaParagraphResource.addProperty(endIndexProperty,
                    nodeEntry.createTypedLiteral(endIndex, XSDDatatype.XSDnonNegativeInteger));

            //Reference Context

            Property resourceContextProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(REFERENCE_CONTEXT));
            dbPediaParagraphResource.addProperty(resourceContextProperty,dbPediaContextUrl);

            Property superStringProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(SUPER_STRING));
            dbPediaParagraphResource.addProperty(superStringProperty,  dbPediaSectionUrl);

            // add hasParagraph to section resource
            Property hasParagraphProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(HAS_PARAGRAPH));
            dbPediaSectionResource.addProperty(hasParagraphProperty,  dbPediaParagraphUrl);

            // add first or last paragraph properties to section resource
            if (!firstParagraphDone) {
                firstParagraphDone = true;
                Property firstParagraphProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(FIRST_PARAGRAPH));
                dbPediaSectionResource.addProperty(firstParagraphProperty,  dbPediaParagraphUrl);
            }
            if (paragraphIndex == numberOfParagraphs - 1) {
                Property lastParagraphProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(LAST_PARAGRAPH));
                dbPediaSectionResource.addProperty(lastParagraphProperty,  dbPediaParagraphUrl);
            }
        }
        //initiate recursion
        List<Subdivision> nodeChildren = node.getChildren();
        for (Subdivision child : nodeChildren) {
            nodeEntry.add(generateNodeEntry(child));
        }
        return nodeEntry;
    }

    public Model generateLinkNodeEntry(ParsedPage parsedPage) {
        Model nodeEntry = ModelFactory.createDefaultModel();
        String articleTitle = encodeSpaces(parsedPage.getTitle());
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

                    Resource dbPediaLinkResource = nodeEntry.createResource(dbPediaLinkUrl);
                    Property rdfSyntaxProperty = nodeEntry.createProperty(RDF_SYNTAX_TYPE);


                    // Link Type NIF
                    Resource contextResource = nodeEntry.createResource(getPersistenceOntologyUrl(linkType.getCapitalizedTypeLabel()));
                    dbPediaLinkResource.addProperty(rdfSyntaxProperty, contextResource);

                    // Reference Context
                    Property referenceContextProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(REFERENCE_CONTEXT));
                    dbPediaLinkResource.addProperty(referenceContextProperty, dbPediaContextUrl);
                    //NIF Indexes
                    Property beginIndexProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(BEGIN_INDEX));
                    dbPediaLinkResource.addProperty(beginIndexProperty, nodeEntry.createTypedLiteral(linkBeginIndex, XSDDatatype.XSDnonNegativeInteger));

                    Property endIndexProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(END_INDEX));
                    dbPediaLinkResource.addProperty(endIndexProperty, nodeEntry.createTypedLiteral(linkEndIndex, XSDDatatype.XSDnonNegativeInteger));
                    // Super String
                    Property superStringProperty = nodeEntry.createProperty(getPersistenceOntologyUrl(SUPER_STRING));
                    dbPediaLinkResource.addProperty(superStringProperty, dbPediaParagraphUrl);
                    // taIdentRef
                    String dbPediaIdentRef = String.format("%s/%s", DBPEDIA_LINK, link.getTaIdentRef());
                    Resource identRefResource = nodeEntry.createResource(dbPediaIdentRef);
                    Property taIdentRefProperty = nodeEntry.createProperty(TA_IDENT_REF);
                    dbPediaLinkResource.addProperty(taIdentRefProperty, identRefResource);
                    // anchorOf
                    Property anchorOfProperty = nodeEntry.createProperty(getPersistenceOntologyUrl("anchorOf"));
                    dbPediaLinkResource.addProperty(anchorOfProperty, link.getAnchorOf());
                }
            }

            List<Subdivision> children = node.getChildren();
            Collections.reverse(children);
            for (Subdivision child : children) {
                nodeStack.push(child);
            }
            Collections.reverse(children);
        }
        return nodeEntry;
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

    private String encodeSpaces(String line) {
        String encodedTitle = line;
        try {
            encodedTitle = URLEncoder.encode(line, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedTitle;
    }
}
