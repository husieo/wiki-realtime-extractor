package org.dbpedia.extractor.service.remover.language;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import org.dbpedia.extractor.entity.xml.Page;
import org.dbpedia.extractor.entity.xml.Siteinfo;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LanguageContainer {


    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "language")
    private List<DynamicLanguage> languages = new ArrayList<>();

    @SuppressWarnings(value = "unused")
    public void setLanguages(List<DynamicLanguage> values){
        languages.addAll(values);
    }
}
