package org.dbpedia.extractor.controller;


import org.dbpedia.extractor.service.XmlDumpParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class ParseController {

    private final XmlDumpParser parser;

    public ParseController(XmlDumpParser parser) {
        this.parser = parser;
    }

    @PostMapping(value = "submit",
            consumes = MediaType.APPLICATION_XML_VALUE)
    public String submitXml(@RequestBody String dump) throws IOException {
        return parser.parseXmlDump(dump).toString();
    }

}
