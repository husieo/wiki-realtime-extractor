package org.dbpedia.extractor.controller;


import org.dbpedia.extractor.parser.WikipediaPageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/")
public class ParseController {

    @Autowired
    private WikipediaPageParser parser;

    @GetMapping("/paragraphs/{title}")
    public List<String> getParagraphs(@PathVariable(value = "title")String title) throws IOException {
        return parser.parsePage(title).getParagraphs();
    }
}
