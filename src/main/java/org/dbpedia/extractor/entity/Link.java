package org.dbpedia.extractor.entity;

import lombok.*;

/**
 * NIF Link
 */
@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class Link {

    @NonNull
    private Position position;

    @NonNull
    private LinkType linkType;

    /**
     * Link text
     */
    @NonNull
    private String anchorOf;

    @ToString.Exclude
    private Paragraph superString;
}
