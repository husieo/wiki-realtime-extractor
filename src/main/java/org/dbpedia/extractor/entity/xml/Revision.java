package org.dbpedia.extractor.entity.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Revision {
    private int id;
    private int parentid;
    private String timestamp;
    private String text;
}
