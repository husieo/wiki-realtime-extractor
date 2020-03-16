package org.dbpedia.extractor.entity.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Siteinfo {
    private String sitename;
    private String dbname;
    private String base;
    private String generator;
    private Namespaces NamespacesObject;

}
