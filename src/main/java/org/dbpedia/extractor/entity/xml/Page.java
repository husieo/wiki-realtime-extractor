package org.dbpedia.extractor.entity.xml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Page {
    private String title;
    private Revision revision;
}
