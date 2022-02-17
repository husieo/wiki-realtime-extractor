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
        Assert.assertFalse(HTML_TAGS.matcher(refText).find());
    }

    @Test
    public void removeMathTest(){
        String refText = ":&lt;math&gt;\\mathrm{^{226}_{\\ 88}Ra\\ +\\ ^{1}_{0}n\\ \\longrightarrow \\ ^{227}_{\\ 88}Ra\\ \\xrightarrow[42,2 \\ min]{\\beta^-} \\ ^{227}_{\\ 89}Ac}&lt;/math&gt;";

        String wikiTagToRemove = "math&gt;";
        refText = wikiTagsRemover.removeMath(refText);
        Assert.assertFalse(refText.contains(wikiTagToRemove));
    }

    @Test
    public void removeMathFormulaTest(){
        String refText = "\\alpha_\\ce{H2A}   &amp;= \\frac{\\ce{[H+]^2}}{\\ce{[H+]^2}  + [\\ce{H+}]K_1 + K_1 K_2}  = \\frac{\\ce{[H2A]}}{\\ce{{[H2A]}} + [HA^-] + [A^{2-}]}\\";
        String wikiTagToRemove = "\\alpha_\\ce";
        refText = wikiTagsRemover.removeMathFormula(refText);
        Assert.assertFalse(refText.contains(wikiTagToRemove));
    }
}