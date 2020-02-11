package org.dbpedia.extractor.controller;


import org.dbpedia.extractor.page.WikipediaPageParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/")
public class ParseController {

    private final WikipediaPageParser parser;

    public ParseController(WikipediaPageParser parser) {
        this.parser = parser;
    }

    @GetMapping(value = "/paragraphs/{title}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getParagraphs(@PathVariable(value = "title")String title) throws IOException {
        return parser.parsePage(title).getParagraphs();
    }


}
