package org.dbpedia.extractor.controller;


import org.dbpedia.extractor.service.WikipediaPageParser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ParseController {

    private final WikipediaPageParser parser;

    public ParseController(WikipediaPageParser parser) {
        this.parser = parser;
    }

//    @GetMapping(value = "/paragraphs/{title}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public List<String> getParagraphs(@PathVariable(value = "title")String title) throws IOException {
//        return parser.parsePage(title).getParagraphs();
//    }


}
