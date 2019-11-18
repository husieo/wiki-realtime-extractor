package org.dbpedia.extractor.parser;

import lombok.Getter;

@Getter
public enum ArticleElements {
    TITLE("firstHeading");

    private String value;

    ArticleElements(String value) {
        this.value = value;
    }

}
