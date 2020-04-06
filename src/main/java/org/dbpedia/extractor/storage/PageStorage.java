package org.dbpedia.extractor.storage;

import lombok.Getter;
import lombok.Setter;
import org.dbpedia.extractor.entity.ParsedPage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
public class PageStorage {
    Map<String, ParsedPage> pageMap = new HashMap<>();

    public void putPage(ParsedPage parsedPage){
        pageMap.put(parsedPage.getTitle(), parsedPage);
    }

    public ParsedPage getPage(String title){
        return pageMap.get(title);
    }
}
