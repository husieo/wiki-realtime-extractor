package org.dbpedia.extractor.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Paragraph {
    private Position position;


    private List<Link> links;

    public Paragraph(int start, int end){
        position = new Position(start, end);
    }
}
