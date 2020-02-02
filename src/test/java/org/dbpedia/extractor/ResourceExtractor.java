package org.dbpedia.extractor;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

import static com.google.common.io.Resources.*;

public class ResourceExtractor {

    public static String asString(String resource) {
        URL url = getResource(resource);
        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
