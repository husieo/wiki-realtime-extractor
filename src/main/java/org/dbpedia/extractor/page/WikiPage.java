package org.dbpedia.extractor.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class WikiPage {
    private String title;
    private String text;
}
