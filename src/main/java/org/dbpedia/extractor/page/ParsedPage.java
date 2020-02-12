package org.dbpedia.extractor.page;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class ParsedPage {
    private String title;
    private List<String> paragraphs;

    /**
     * Page structure: topics, subtopics, etc.
     */
    private Map<Integer,List<Integer>> pageStructure;
}
