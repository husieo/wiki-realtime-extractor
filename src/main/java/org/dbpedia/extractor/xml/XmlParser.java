package org.dbpedia.extractor.xml;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Getter
@Setter
public class XmlParser {
    String path;
    ArrayList<String> pages;


    public void parseXml(){

    }
}
