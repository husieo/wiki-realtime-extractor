package org.dbpedia.extractor.controller;


import org.dbpedia.extractor.entity.Context;
import org.dbpedia.extractor.entity.Link;
import org.dbpedia.extractor.service.WikipediaPageParser;
import org.dbpedia.extractor.service.XmlDumpParser;
import org.dbpedia.extractor.storage.PageStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/")
public class ParseController {

    private final XmlDumpParser parser;

    private final PageStorage pageStorage;

    public ParseController(XmlDumpParser parser, PageStorage pageStorage) {
        this.parser = parser;
        this.pageStorage = pageStorage;
    }

    @PostMapping(value = "submit",
            consumes = MediaType.APPLICATION_XML_VALUE)
    public String submitXml(@RequestBody String dump) throws IOException {
        return parser.parseXmlDump(dump).toString();
    }

    @GetMapping(value = "/articles/{title}/context")
    public String getContext(@PathVariable String title){
        return pageStorage.getPage(title).getContext().getText();
    }

    @GetMapping(value = "/articles/{title}/structure")
    public String getStructure(@PathVariable String title){
        return pageStorage.getPage(title).getStructureRoot().toString();
    }

    @GetMapping(value = "/articles/{title}/links")
    public String getLinks(@PathVariable String title){
        StringBuilder result = new StringBuilder();
        List<Link> links = pageStorage.getPage(title).getStructureRoot().getLinks();
        Context context = pageStorage.getPage(title).getContext();
        for(Link link : links){
            result.append(link.getNifFormat(context));
        }
        return result.toString();
    }

    @GetMapping(value = "/articles/count")
    public int getArticleCount(){
        return pageStorage.getPageCount();
    }

}
