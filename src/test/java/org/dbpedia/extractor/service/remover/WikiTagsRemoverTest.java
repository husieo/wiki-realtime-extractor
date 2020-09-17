package org.dbpedia.extractor.service.remover;

import lombok.extern.log4j.Log4j;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.regex.Pattern;

@Log4j
class WikiTagsRemoverTest {

    private static WikiTagsRemover wikiTagsRemover;

    @BeforeAll
    public static void beforeAll() throws IOException {
        wikiTagsRemover = new WikiTagsRemover();
    }

    @Test
    public void removeRefTags() {
        String refText = "&lt;ref&gt;{{cite OED|anarchism}}&lt;/ref&gt; and the <ref>word</ref> &lt;ref&gt;{{cite OED|anarchism}}&lt;/ref&gt;";
        refText = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(refText));

        Pattern HTML_TAGS = Pattern.compile("<[^>]+>");
        refText = wikiTagsRemover.removeHtmlTags(refText);
        log.info(refText);
        Assert.assertFalse(HTML_TAGS.matcher(refText).find());
    }
}