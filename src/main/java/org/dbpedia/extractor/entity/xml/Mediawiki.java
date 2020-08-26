package org.dbpedia.extractor.entity.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * XML Classes for simplified XML parsing
 */
@Getter
@Setter
@ToString
public class Mediawiki {
    private Siteinfo siteinfo;
    private String xmlns;
    private String xsi;
    private String schemaLocation;
    private String version;
    private String lang;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "page")
    private List<Page> pages = new ArrayList<>();

    @SuppressWarnings(value = "unused")
    public void setPages(List<Page> values){
        pages.addAll(values);
    }
}