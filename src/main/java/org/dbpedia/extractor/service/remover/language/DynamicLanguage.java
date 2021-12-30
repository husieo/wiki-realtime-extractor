package org.dbpedia.extractor.service.remover.language;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
@ToString
public class DynamicLanguage extends LanguageFooterRemover {

    private String langName;

    private String categoryName;

    private String isoLangCode;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "footer")
    private List<String> footers = new ArrayList<>();


    @Override
    protected List<Pattern> categoryLinkPatterns() {
        return categoryLinkPatterns(categoryName);
    }

    @Override
    protected List<Pattern> footerPatterns() {
        return footerPatterns(footers.toArray(new String[0]));
    }

    @SuppressWarnings(value = "unused")
    public void setFooters(List<String> values){
        footers.addAll(values);
    }
}
