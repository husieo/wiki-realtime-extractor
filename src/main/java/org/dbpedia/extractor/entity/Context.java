package org.dbpedia.extractor.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Context {
    private String text;

    public String getText(Position position){
        return text.substring(position.getStart(),position.getEnd());
    }
}
