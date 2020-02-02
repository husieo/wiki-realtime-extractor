package org.dbpedia.extractor.loader;

import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@CommonsLog
@Service
public class PageLoader {

    public Document readPage(String title) throws IOException {
        return Jsoup.connect(buildUrl(title)).get();
    }

    private String buildUrl(String title){
        String url = "http://en.wikipedia.org/";
        return url + "?title=" + title + "&action=raw";
    }
}
