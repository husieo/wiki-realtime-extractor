package org.dbpedia.extractor.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.dbpedia.exception.ParsingException;
import org.dbpedia.extractor.entity.ParsedPage;
import org.dbpedia.extractor.entity.WikiPage;
import org.dbpedia.extractor.entity.xml.Mediawiki;
import org.dbpedia.extractor.entity.xml.Page;
import org.dbpedia.extractor.entity.xml.Revision;
import org.dbpedia.extractor.service.remover.language.WikiLanguages;
import org.dbpedia.extractor.storage.PageStorage;
import org.dbpedia.extractor.writer.OutputFolderWriter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    ///// NOT USED (IN ITERATIVE PARSE)
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

    public void iterativeParseXmlDump(String filePath, String outputFolder) throws IOException {
        OutputFolderWriter outputFolderWriter = new OutputFolderWriter(outputFolder);
        File xmlDumpFile = new File(filePath);
        LineIterator it = FileUtils.lineIterator(xmlDumpFile, "UTF-8");
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
                    WikiPage wikiPage = new WikiPage(page.getTitle(), revision.getText());
                    ParsedPage parsedPage = null;
                    try {
                        parsedPage = wikipediaPageParser.parsePage(wikiPage);
                        log.info("Parsed page: " + parsedPage.getTitle());
                        String contextEntry = nifFormatter.generateContextEntry(parsedPage);
                        outputFolderWriter.writeToFile(OutputFolderWriter.CONTEXT_FILENAME, contextEntry);
                        String pageStructEntry = nifFormatter.generatePageStructureEntry(parsedPage);
                        outputFolderWriter.writeToFile(OutputFolderWriter.STRUCTURE_FILENAME, pageStructEntry);
                        String linksEntry = nifFormatter.generateLinksEntry(parsedPage);
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
            int total = Math.max(success + failure, 1);
            double successRate = 100 * (success / (double) total);
            log.info(String.format("Total pages parsed: %d. Success rate: %.2f%%", total, successRate));
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
}
