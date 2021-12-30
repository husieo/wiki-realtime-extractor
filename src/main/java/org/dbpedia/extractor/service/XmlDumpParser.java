package org.dbpedia.extractor.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.jena.rdf.model.Model;
import org.dbpedia.exception.ParsingException;
import org.dbpedia.extractor.entity.ParsedPage;
import org.dbpedia.extractor.entity.WikiPage;
import org.dbpedia.extractor.entity.xml.Mediawiki;
import org.dbpedia.extractor.entity.xml.Page;
import org.dbpedia.extractor.entity.xml.Revision;
import org.dbpedia.extractor.storage.PageStorage;
import org.dbpedia.extractor.writer.OutputFolderWriter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Service
@Log4j
public class XmlDumpParser {

    private final WikipediaPageParser wikipediaPageParser;

    private PageStorage pageStorage;

    private XmlMapper xmlMapper;

    private NifFormatter nifFormatter;


    private static final String pageBegin = "<page>";
    private static final String pageEnd = "</page>";

    public XmlDumpParser(WikipediaPageParser wikipediaPageParser, PageStorage pageStorage, NifFormatter nifFormatter) {
        this.wikipediaPageParser = wikipediaPageParser;
        this.pageStorage = pageStorage;
        this.nifFormatter = nifFormatter;
        xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Async
    public CompletableFuture<List<ParsedPage>> asyncParseXmlDump(String xmlDump) throws IOException {
        return CompletableFuture.completedFuture(parseXmlDump(xmlDump));
    }

    ///// NOT USED (IN ITERATIVE PARSE). ONLY FOR REST API
    public List<ParsedPage> parseXmlDump(String xmlDump) throws IOException {
        Mediawiki mediawiki = deserializeXml(xmlDump);
        List<Page> pages = mediawiki.getPages();
        List<ParsedPage> parsedPages = new ArrayList<>();
        for (Page page : pages) {
            Revision revision = page.getRevision();
            WikiPage wikiPage = new WikiPage(page.getTitle(), revision.getText());
            try {
                parsedPages.add(wikipediaPageParser.parsePage(wikiPage));
            } catch (ParsingException e) {
                log.error(String.format("Error parsing page %s: %s", page.getTitle(), e.getMessage()));
            }
        }
        for (ParsedPage parsedPage : parsedPages) {
            log.info("Parsed page: " + parsedPage.getTitle());
            pageStorage.putPage(parsedPage);
        }
        return parsedPages;
    }

    public void iterativeParseXmlDump(String filePath, String outputFolder) throws IOException{
        File xmlDumpFile = new File(filePath);
        iterativeParseXmlDump(xmlDumpFile, outputFolder);
    }

    public void iterativeParseXmlDump(File xmlDumpFile, String outputFolder) throws IOException  {
        OutputFolderWriter outputFolderWriter = new OutputFolderWriter(outputFolder);
        LineIterator it = FileUtils.lineIterator(xmlDumpFile, "UTF-8");
        Instant parsingStart = Instant.now();
        int success = 0;
        int failure = 0;
        try {
            StringBuilder pageString = new StringBuilder();
            boolean pageStarted = false;
            while (it.hasNext()) {
                String line = it.nextLine();
                if (line.contains(pageBegin) && !pageStarted) { // start page
                    pageString = new StringBuilder();
                    pageString.append(line).append(System.lineSeparator());
                    pageStarted = true;
                } else if (pageStarted && line.contains(pageEnd)) { //end page

                    pageString.append(line).append(System.lineSeparator());
                    Page page = deserializePage(pageString.toString());
                    Revision revision = page.getRevision();
                    if(isPageRedirect(page)){ // skip if it is page redirect
                        pageStarted = false;
                        continue;
                    }
                    WikiPage wikiPage = new WikiPage(page.getTitle(), revision.getText());
                    ParsedPage parsedPage = null;
                    try {
                        parsedPage = wikipediaPageParser.parsePage(wikiPage);
                        log.info("Parsed page: " + parsedPage.getTitle());
                        Model contextEntry = nifFormatter.generateContextEntry(parsedPage);
                        outputFolderWriter.writeToFile(OutputFolderWriter.CONTEXT_FILENAME, contextEntry);
                        Model pageStructEntry = nifFormatter.generatePageStructureEntry(parsedPage);
                        outputFolderWriter.writeToFile(OutputFolderWriter.STRUCTURE_FILENAME, pageStructEntry);
                        Model linksEntry = nifFormatter.generateLinksEntry(parsedPage);
                        outputFolderWriter.writeToFile(OutputFolderWriter.LINKS_FILENAME, linksEntry);
                        success++;
                    } catch (ParsingException e) {
                        log.error(String.format("Error parsing page %s: %s", wikiPage.getTitle(), e.getMessage()));
                        failure++;
                    } finally {
                        pageStarted = false;
                    }
                } else if (pageStarted) { // write down a line
                    pageString.append(line).append(System.lineSeparator());
                }
            }
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(parsingStart, finish).toSeconds();

            int total = Math.max(success + failure, 1);
            double successRate = 100 * (success / (double) total);
            log.info(String.format("Total pages parsed: %d. Success rate: %.2f%%. Seconds passed: %d", total, successRate, timeElapsed));
        } finally {
            LineIterator.closeQuietly(it);
        }
    }


    private Mediawiki deserializeXml(String dump) throws IOException {
        return xmlMapper.readValue(dump, Mediawiki.class);
    }

    private Page deserializePage(String pageString) throws IOException {
        return xmlMapper.readValue(pageString, Page.class);
    }

    /**
     * Check if the page is a simple redirect and contains no data.
     * @param page Page to be checked.
     * @return True if page has #REDIRECT tag.
     */
    private boolean isPageRedirect(Page page) {
        return Pattern.compile("#REDIRECT", Pattern.CASE_INSENSITIVE + Pattern.LITERAL)
                .matcher(page.getRevision().getText()).find();
    }
}
