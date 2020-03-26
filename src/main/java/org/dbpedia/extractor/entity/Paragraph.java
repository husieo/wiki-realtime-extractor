package org.dbpedia.extractor.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Paragraph {
    Position position;

    public Paragraph(int start, int end){
        position = new Position(start, end);
    }
}
