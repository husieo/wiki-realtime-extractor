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

    private Position position;

    @NonNull
    private LinkType linkType;

    /**
     * Link text
     */
    @NonNull
    @ToString.Exclude
    private String anchorOf;

    @ToString.Exclude
    private String taIdentRef;

    @ToString.Exclude
    private Paragraph superString;

    public String getNifFormat(Context context){
        return toString() + "text: " + context.getText(position) + System.lineSeparator();
    }
}
