package org.dbpedia.extractor.service;

import org.dbpedia.extractor.entity.ParsedPage;
import org.dbpedia.extractor.service.remover.language.LanguageIdentifierBean;
import org.dbpedia.extractor.storage.PageStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class XmlDumpService {

    private final XmlDumpParser parser;

    private final PageStorage pageStorage;

    private final NifFormatter nifFormatter;

    public XmlDumpService(XmlDumpParser parser, PageStorage pageStorage, NifFormatter nifFormatter) {
        this.parser = parser;
        this.pageStorage = pageStorage;
        this.nifFormatter = nifFormatter;
    }

    public void submitXml(String inputFilePath, String outputFolder, boolean dryRun) throws IOException {
        parser.setDryRun(dryRun);
        parser.iterativeParseXmlDump(inputFilePath, outputFolder);
    }

    // was used in the service previously
//    public String getNifContext(){
//        Map<String, ParsedPage> pageMap = pageStorage.getPageMap();
//        StringBuilder output = new StringBuilder();
//        System.out.println(pageMap.size());
//        for(ParsedPage page : pageMap.values()) {
//            output.append(nifFormatter.generateContextEntry(page));
//        }
//        return output.toString();
//    }

}
