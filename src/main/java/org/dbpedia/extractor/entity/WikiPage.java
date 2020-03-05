package org.dbpedia.extractor.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class WikiPage {
    @NonNull
    private String title;
    @NonNull
    private String text;

    private List<Position> paragraphPositions;

    public String getText(Position position){
        return text.substring(position.getStart(),position.getEnd());
    }
}
