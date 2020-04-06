package org.dbpedia.extractor.entity;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class ParsedPage {

    /**
     * NIF context
     */
    private Context context;

    /**
     * Page structure: topics, subtopics, etc.
     */
    private Subdivision structureRoot;

    private WikiPage wikiPage;

    public String getTitle(){
        return wikiPage.getTitle();
    }
}
