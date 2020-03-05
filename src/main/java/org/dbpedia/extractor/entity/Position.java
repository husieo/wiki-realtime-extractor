package org.dbpedia.extractor.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Class for accessing string positions in a text
 */
@Getter
@Setter
@AllArgsConstructor
public class Position {
    private int start;
    private int end;
}
