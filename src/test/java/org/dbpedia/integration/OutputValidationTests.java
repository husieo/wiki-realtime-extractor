package org.dbpedia.integration;

import lombok.extern.log4j.Log4j;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.dbpedia.extractor.service.NifFormatter;
import org.dbpedia.extractor.service.WikipediaPageParser;
import org.dbpedia.extractor.service.XmlDumpParser;
import org.dbpedia.extractor.service.remover.WikiTagsRemover;
import org.dbpedia.extractor.service.remover.language.LanguageFooterRemover;
import org.dbpedia.extractor.service.remover.language.LanguageIdentifierBean;
import org.dbpedia.extractor.service.transformer.ContextLanguageTransformer;
import org.dbpedia.extractor.storage.PageStorage;
import org.dbpedia.extractor.writer.OutputFolderWriter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log4j
public class OutputValidationTests {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    private static WikipediaPageParser pageParser;
    private static LanguageIdentifierBean languageIdentifier;
    private static WikiTagsRemover wikiTagsRemover;
    private static LanguageFooterRemover languageFooterRemover;
    private static PageStorage pageStorage;
    private static NifFormatter nifFormatter;
    private static XmlDumpParser xmlDumpParser;
    private Model jenaModel;

    private static final String NTRIPLES = "N-TRIPLES";

    private static final String PERSISTENCE_ONTOLOGY_LINK = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core";

    @BeforeAll
    public static void beforeAll() {
        pageParser = new WikipediaPageParser(new ContextLanguageTransformer());
        languageIdentifier = new LanguageIdentifierBean();
        wikiTagsRemover = new WikiTagsRemover();
        pageParser.setLanguageIdentifierBean(languageIdentifier);
        languageIdentifier.readLanguageList();
        pageParser.setWikiTagsRemover(wikiTagsRemover);

        pageStorage = new PageStorage();
        nifFormatter = new NifFormatter();
        nifFormatter.setLanguageIdentifierBean(languageIdentifier);
        xmlDumpParser = new XmlDumpParser(pageParser, pageStorage, nifFormatter);
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        folder.create();
        jenaModel = ModelFactory.createDefaultModel();
    }

    @Test
    public void testOutputExists() throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File articlesFile = new File(classLoader.getResource("xml_test_page.xml").getFile());
        languageIdentifier.setLanguage("ENGLISH");
        languageFooterRemover = languageIdentifier.getLanguage();
        wikiTagsRemover.setLanguageFooterRemover(languageFooterRemover);
        xmlDumpParser.iterativeParseXmlDump(articlesFile, folder.getRoot().getPath());

        Assert.assertTrue(Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.CONTEXT_FILENAME).toFile().exists());
        Assert.assertTrue(Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.LINKS_FILENAME).toFile().exists());
        Assert.assertTrue(Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.STRUCTURE_FILENAME).toFile().exists());
    }

    @Test
    public void testEnglishArticleGeneral() throws IOException {
        loadArticle("xml_test_page.xml", "ENGLISH");

        File outputContext = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.CONTEXT_FILENAME).toFile();
        File outputLinks = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.LINKS_FILENAME).toFile();
        File outputStructure = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.STRUCTURE_FILENAME).toFile();

        jenaModel.read(new FileInputStream(outputContext), null, NTRIPLES);

        // verify total context size is 6 triples
        Assert.assertEquals(6, jenaModel.size());
        jenaModel.removeAll();
        jenaModel.read(new FileInputStream(outputLinks), null, NTRIPLES);
        Assert.assertEquals(3073, jenaModel.size());
        jenaModel.removeAll();
        jenaModel.read(new FileInputStream(outputStructure), null, NTRIPLES);
        Assert.assertEquals(429, jenaModel.size());
        jenaModel.removeAll();
    }


    @Test
    public void testEnglishArticleContext() throws IOException {
        loadArticle("xml_test_page.xml", "ENGLISH");

        File outputContext = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.CONTEXT_FILENAME).toFile();

        jenaModel.read(new FileInputStream(outputContext), null, NTRIPLES);

        String testSubject = String.format("http://dbpedia.org/resource/Anarchism?dbpv=%s&nif=context"
                , getCurrentDateString());
        String testPredicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String testObject = getPersistenceOntologyUrl("Context");

        // test if type is Context
        assertStatementContains(testSubject, testPredicate, testObject);

        testPredicate = getPersistenceOntologyUrl("beginIndex");
        Literal testObjectLiteral = createTypedLiteral("0", XSDDatatype.XSDnonNegativeInteger);

        // test beginIndex
        assertStatementContains(testSubject, testPredicate, testObjectLiteral);

        testPredicate = getPersistenceOntologyUrl("endIndex");
        testObjectLiteral = createTypedLiteral("43346", XSDDatatype.XSDnonNegativeInteger);

        // test endIndex
        assertStatementContains(testSubject, testPredicate, testObjectLiteral);

        testPredicate = getPersistenceOntologyUrl("predLang");
        testObjectLiteral = createLiteral("http://lexvo.org/id/iso639-3/eng");

        // test predLang
        assertStatementContains(testSubject, testPredicate, testObjectLiteral);

        testPredicate = getPersistenceOntologyUrl("sourceUrl");
        testObjectLiteral = createLiteral("http://en.wikipedia.org/wiki/Anarchism");

        //test sourceUrl
        assertStatementContains(testSubject, testPredicate, testObjectLiteral);

        testPredicate = getPersistenceOntologyUrl("isString");
        Model referenceModel = ModelFactory.createDefaultModel();
        Resource subjectRes =
                referenceModel.createResource(testSubject);
        Property predicateProperty = referenceModel.createProperty(testPredicate);

        // test if there is a context text
        Assert.assertTrue(jenaModel.contains(subjectRes, predicateProperty));


        jenaModel.removeAll();
    }

    @Test
    public void testEnglishArticleLinks() throws IOException {
        loadArticle("xml_test_page.xml", "ENGLISH");

        File outputLinks = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.LINKS_FILENAME).toFile();

        jenaModel.read(new FileInputStream(outputLinks), null, NTRIPLES);

        String dbPediaArticleLink = String.format("http://dbpedia.org/resource/Anarchism?dbpv=%s", getCurrentDateString());

        String testSubject = String.format("%s&nif=phrase_49_66", dbPediaArticleLink);
        String testPredicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String testObject = getPersistenceOntologyUrl("Phrase");

        // test if phrase social philosophy is in the links
        assertStatementContains(testSubject, testPredicate, testObject);

        testPredicate = getPersistenceOntologyUrl("referenceContext");
        Literal testObjectLiteral =
                createLiteral(String.format("%s&nif=context", dbPediaArticleLink));
        // test reference context
        assertStatementContains(testSubject, testPredicate, testObjectLiteral);

        testPredicate = getPersistenceOntologyUrl("beginIndex");
        testObjectLiteral = createTypedLiteral("49", XSDDatatype.XSDnonNegativeInteger);

        // test beginIndex
        assertStatementContains(testSubject, testPredicate, testObjectLiteral);

        testPredicate = getPersistenceOntologyUrl("endIndex");
        testObjectLiteral = createTypedLiteral("66", XSDDatatype.XSDnonNegativeInteger);

        // test endIndex
        assertStatementContains(testSubject, testPredicate, testObjectLiteral);

        testPredicate = getPersistenceOntologyUrl("superString");
        testObjectLiteral =
                createLiteral(String.format("%s&nif=paragraph_0_550", dbPediaArticleLink));
        // test super(parent) string
        assertStatementContains(testSubject, testPredicate, testObjectLiteral);

        testPredicate = "http://www.w3.org/2005/11/its/rdf#taIdentRef";
        testObject = "http://dbpedia.org/resource/Social_philosophy";

        // test if phrase social philosophy has an identity reference
        assertStatementContains(testSubject, testPredicate, testObject);

        testPredicate = getPersistenceOntologyUrl("anchorOf");
        testObjectLiteral =
                createLiteral("social philosophy");
        // test anchor of
        assertStatementContains(testSubject, testPredicate, testObjectLiteral);

        jenaModel.removeAll();
    }

    @Test
    public void testEnglishArticleStructure() throws IOException {
        loadArticle("xml_test_page.xml", "ENGLISH");

        File outputLinks = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.LINKS_FILENAME).toFile();

        jenaModel.read(new FileInputStream(outputLinks), null, NTRIPLES);

        String dbPediaArticleLink = String.format("http://dbpedia.org/resource/Anarchism?dbpv=%s", getCurrentDateString());

        String testSubject = String.format("%s&nif=phrase_49_66", dbPediaArticleLink);
        String testPredicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String testObject = getPersistenceOntologyUrl("Phrase");

        // test if phrase social philosophy is in the links
        assertStatementContains(testSubject, testPredicate, testObject);

        jenaModel.removeAll();
    }

    @Test
    public void testGermanArticleGeneral() throws IOException {
        loadArticle("xml_test_deutsch_one_page.xml", "GERMAN");

        File outputContext = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.CONTEXT_FILENAME).toFile();
        File outputLinks = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.LINKS_FILENAME).toFile();
        File outputStructure = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.STRUCTURE_FILENAME).toFile();

        jenaModel.read(new FileInputStream(outputContext), null, NTRIPLES);

        // verify total context size is 6 triples
        Assert.assertEquals(6, jenaModel.size());
        jenaModel.removeAll();
        jenaModel.read(new FileInputStream(outputLinks), null, NTRIPLES);
        Assert.assertEquals(504, jenaModel.size());
        jenaModel.removeAll();
        jenaModel.read(new FileInputStream(outputStructure), null, NTRIPLES);
        Assert.assertEquals(151, jenaModel.size());
        jenaModel.removeAll();
    }

    @Test
    public void testGermanArticleContext() throws IOException{
        loadArticle("xml_test_deutsch_one_page.xml", "GERMAN");

        File outputLinks = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.CONTEXT_FILENAME).toFile();

        jenaModel.read(new FileInputStream(outputLinks), null, NTRIPLES);

        String testSubject = String.format("http://dbpedia.org/resource/Actinium?dbpv=%s&nif=context", getCurrentDateString());
        String testPredicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String testObject = getPersistenceOntologyUrl("Context");

        // test if type is Context
        assertStatementContains(testSubject, testPredicate, testObject);

        testPredicate = getPersistenceOntologyUrl("predLang");
        Literal testObjectLiteral = createLiteral("http://lexvo.org/id/iso639-3/deu");

        // test predLang
        assertStatementContains(testSubject, testPredicate, testObjectLiteral);
    }

    @Test
    public void testGermanArticleLinks() throws IOException {
        loadArticle("xml_test_deutsch_one_page.xml", "GERMAN");

        File outputLinks = Paths.get(folder.getRoot().getAbsolutePath(), OutputFolderWriter.LINKS_FILENAME).toFile();

        jenaModel.read(new FileInputStream(outputLinks), null, NTRIPLES);

        String dbPediaArticleLink = String.format("http://dbpedia.org/resource/Actinium?dbpv=%s", getCurrentDateString());

        String testSubject = String.format("%s&nif=word_5165_5185", dbPediaArticleLink);
        String testPredicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String testObject = getPersistenceOntologyUrl("Word");

        // test if word Aktivierungsanalysen is in the links
        assertStatementContains(testSubject, testPredicate, testObject);
    }



    /**
     * Test if statement is in the Apache Model. Overloaded for string inputs.
     */
    private void assertStatementContains(String testSubject, String testPredicate, String testObject) {
        assertStatementContains(createStatement(testSubject, testPredicate, testObject));
    }

    /**
     * Test if statement with literal is in the Apache Model. Overloaded for Literal inputs.
     */
    private void assertStatementContains(String testSubject, String testPredicate, Literal testObjectLiteral) {
        assertStatementContains(createStatementWithLiteral(testSubject, testPredicate, testObjectLiteral));
    }

    /**
     * Test if statement is in the Apache Model
     */
    private void assertStatementContains(Statement statement) {
        Assert.assertTrue(String.format("Statement:\"%s\" not found in the model.",statement.toString())
                , jenaModel.contains(statement));
    }

    /**
     * Load and parse an article from XML file
     *
     * @param fileName XML file to be parsed
     * @param language Article's language
     */
    private void loadArticle(String fileName, String language) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File articlesFile = new File(classLoader.getResource(fileName).getFile());
        languageIdentifier.setLanguage(language);
        languageFooterRemover = languageIdentifier.getLanguage();
        wikiTagsRemover.setLanguageFooterRemover(languageFooterRemover);
        xmlDumpParser.iterativeParseXmlDump(articlesFile, folder.getRoot().getPath());
    }

    private String getCurrentDateString() {
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        return simpleDateFormat.format(currentDate);
    }

    /**
     * Create an RDF statement
     *
     * @param subjString Subject
     * @param predString Predicate
     * @param objString  Object
     * @return Apache Jena Statement
     */
    private Statement createStatement(String subjString, String predString, String objString) {
        Model referenceModel = ModelFactory.createDefaultModel();
        Resource testSubject =
                referenceModel.createResource(subjString);
        Property testPredicate = referenceModel.createProperty(predString);
        Resource testObject = referenceModel.createResource(objString);

        return referenceModel.createStatement(testSubject, testPredicate, testObject);
    }

    /**
     * Create an RDF statement with a literal
     *
     * @param subjString Subject
     * @param predString Predicate
     * @param objLiteral Object Literal
     * @return Apache Jena Statement
     */
    private Statement createStatementWithLiteral(String subjString, String predString, Literal objLiteral) {
        Model referenceModel = ModelFactory.createDefaultModel();
        Resource testSubject =
                referenceModel.createResource(subjString);
        Property testPredicate = referenceModel.createProperty(predString);

        return referenceModel.createStatement(testSubject, testPredicate, objLiteral);
    }

    /**
     * Create typed Literal
     *
     * @param literalString Literal
     * @return Apache Jena literal
     */
    private Literal createTypedLiteral(String literalString, XSDDatatype literalType) {
        Model referenceModel = ModelFactory.createDefaultModel();
        return referenceModel.createTypedLiteral(Integer.valueOf(literalString), literalType);
    }

    private Literal createLiteral(String literalString) {
        Model referenceModel = ModelFactory.createDefaultModel();
        return referenceModel.createLiteral(literalString);
    }

    private String getPersistenceOntologyUrl(String ontologyType) {
        return String.format("%s#%s", PERSISTENCE_ONTOLOGY_LINK, ontologyType);
    }
}
