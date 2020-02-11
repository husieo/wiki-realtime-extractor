package org.dbpedia.extractor.page;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
//TODO NIF  format
public class ParsedPage {
    private String title;
    private List<String> paragraphs;
}
