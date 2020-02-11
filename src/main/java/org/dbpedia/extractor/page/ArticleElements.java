package org.dbpedia.extractor.page;

import lombok.Getter;

@Getter
public enum ArticleElements {
    TITLE("firstHeading");

    private String value;

    ArticleElements(String value) {
        this.value = value;
    }

}
