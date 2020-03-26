package org.dbpedia.extractor.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class for accessing string positions in a text
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class Position {
    private int start;
    private int end;
}
