package org.dbpedia.extractor.service;

import lombok.extern.log4j.Log4j;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.impl.IRIImpl;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.vocabulary.VCARD;
import org.dbpedia.extractor.entity.LinkType;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.ls.LSOutput;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@Log4j
class NifFormatterTest {

    /**
     * Test If Apache JEna buils International resource identifier(IRI) correctly
     */
    @Test
    public void testApacheJenaIRI() {
        //GIVEN
        String dbPediaLinkUrl = "http://dbpedia.org/resource/Anarchism?dbpv=2020-10&nif=word_16_34";
        String rdfSyntaxType = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
        String persistenceOntologyLink = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core";
        LinkType wordLinkType = LinkType.WORD;

        //WHEN
        Model model = ModelFactory.createDefaultModel();
        Resource dbPediaResource = model.createResource(dbPediaLinkUrl);
        Resource wordResource = model.createResource(persistenceOntologyLink + "#" + wordLinkType.getCapitalizedTypeLabel());
        Property property = model.createProperty(rdfSyntaxType, "#type");
        dbPediaResource.addProperty(property, wordResource);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        model.write(outputStream, "NTRIPLE");

        //THEN
        String modelResult = new String(outputStream.toByteArray());
        String idealOutput = "<http://dbpedia.org/resource/Anarchism?dbpv=2020-10&nif=word_16_34> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Word> .\n";
        Assert.assertEquals(idealOutput, modelResult);
    }

    /**
     * Test if Apache Jena builds literals correctly
     */
    @Test
    public void testApacheJenaLiteral() {

        //GIVEN
        String dbPediaLinkUrl = "http://dbpedia.org/resource/Anarchism?dbpv=2020-10&nif=word_16_34";
        String persistenceOntologyLink = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core";

        //WHEN
        Model model = ModelFactory.createDefaultModel();
        Resource dbPediaResource = model.createResource(dbPediaLinkUrl);
        Property property = model.createProperty(persistenceOntologyLink, "#anchorOf");
        dbPediaResource.addProperty(property, "anti-authoritarian");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        model.write(outputStream, "NTRIPLE");

        //THEN
        String modelResult = new String(outputStream.toByteArray());
        String idealOutput = "<http://dbpedia.org/resource/Anarchism?dbpv=2020-10&nif=word_16_34> <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> \"anti-authoritarian\" .\n";
        Assert.assertEquals(idealOutput, modelResult);
    }

    /**
     * Test if Apache Jena builds typed literals correctly
     */
    @Test
    public void testApacheJenaTypedLiteral() {

        //GIVEN
        String dbPediaLinkUrl = "http://dbpedia.org/resource/Anarchism?dbpv=2020-10&nif=word_16_34";
        String persistenceOntologyLink = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core";

        //WHEN
        Model model = ModelFactory.createDefaultModel();
        Resource dbPediaResource = model.createResource(dbPediaLinkUrl);
        Property property = model.createProperty(persistenceOntologyLink, "#beginIndex");
        dbPediaResource.addProperty(property, model.createTypedLiteral(16, XSDDatatype.XSDnonNegativeInteger));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        model.write(outputStream, "NTRIPLE");

        //THEN
        String modelResult = new String(outputStream.toByteArray());
        String idealOutput = "<http://dbpedia.org/resource/Anarchism?dbpv=2020-10&nif=word_16_34> <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex> \"16\"^^<http://www.w3.org/2001/XMLSchema#nonNegativeInteger> .\n";
        Assert.assertEquals(idealOutput, modelResult);
    }


}