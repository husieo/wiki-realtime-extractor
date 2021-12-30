package org.dbpedia.extractor.service.remover;

import org.dbpedia.exception.ParsingException;
import org.dbpedia.extractor.service.BracesMatcher;
import org.dbpedia.extractor.service.remover.language.*;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class WikiTagsRemover {

    private LanguageFooterRemover languageFooterRemover;

    //TODO What if the pattern is not a separate line, but only ends a line? Then [\n]? might break some paragraphs.
    private static final Pattern EMPHASIS = Pattern.compile("('''|'')[\\n]?");
    private static final Pattern HTML_COMMENT = Pattern.compile(
            "(<|&lt;|&#60;)!--.*?--(>|&gt;|&#62;)[\\n]?", Pattern.DOTALL);
    private static final Pattern UNIT_CONVERSION1 =
            Pattern.compile("\\{\\{convert\\|(\\d+)\\|([^|]+)\\}\\}");
    private static final Pattern UNIT_CONVERSION2 =
            Pattern.compile("\\{\\{convert\\|(\\d+)\\|([^|]+)\\|[^}]+\\}\\}");
    private static final Pattern GALLERY = Pattern.compile("&lt;gallery&gt;.*?&lt;/gallery&gt;[\\n]?",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern NO_TOC = Pattern.compile("__NOTOC__[\\n]?");
    private static final Pattern INDENTATION = Pattern.compile("[\\n\\r]:\\s*");
    private static final Pattern MATH = Pattern.compile("&lt;math&gt;.*?&lt;/math&gt;",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static Pattern MATH_FORMULA = Pattern.compile("\\\\alpha_(\\\\ce)?\\{.*\\{.*\\}.*\\}[\\n]?");
    // IPA parenthetical may be enclosed either with parentheses or brackets (de articles).
    private static final Pattern IPA1 = Pattern.compile(" (\\(|\\[)\\{\\{IPA[^\\}]+\\}\\}(\\)|\\])[\\n]?");
    private static final Pattern IPA2 = Pattern.compile(" \\{\\{IPA[^\\}]+\\}\\}[\\n]?");
    //TODO Create a set of all HTML tags that are used in Wikipedia instead. This pattern can catch text that is not HTML tag.
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]{1,20}>");


    public String removeMath(String s) {
        return MATH.matcher(s).replaceAll("");
    }

    /**
     * Remove math formulae
     */
    public String removeMathFormula(String s) { return MATH_FORMULA.matcher(s).replaceAll("");}

    public String fixUnitConversion(String s) {
        String t = UNIT_CONVERSION1.matcher(s).replaceAll("$1 $2");
        return UNIT_CONVERSION2.matcher(t).replaceAll("$1 $2");
    }

    public String removeEmphasis(String s) {
        return EMPHASIS.matcher(s).replaceAll("");
    }

    public String removeHtmlComments(String s) {
        return HTML_COMMENT.matcher(s).replaceAll("");
    }

    public String removeGallery(String s) {
        return GALLERY.matcher(s).replaceAll("");
    }

    public String removeNoToc(String s) {
        return NO_TOC.matcher(s).replaceAll("");
    }

    public String removeIndentation(String s) {
        return INDENTATION.matcher(s).replaceAll("\n");
    }

    public String removeParentheticals(String s) {
        // Take care of things like: id 36
        // '''Albedo''' ({{IPAc-en|icon|æ|l|ˈ|b|iː|d|oʊ}}), or ''reflection coefficient'' ...
        //
        // Note that we shouldn't just leave this to the double-curly remover, since that would leave
        // the dangling empty parentheses.
        s = IPA1.matcher(s).replaceAll("");

        // Straight-up IPA, with no parenthetical.
        s = IPA2.matcher(s).replaceAll("");

        return s;
    }

    public String removeHtmlTags(String s) {
        return HTML_TAGS.matcher(s).replaceAll("");
    }

    public String removeFooter(String s) {
        return languageFooterRemover.removeFooter(s);
    }

    public String removeCategoryLinks(String s) {
        return languageFooterRemover.removeCategoryLinks(s);
    }

    public String removeCites(String s) throws ParsingException {
        String citesStart = "{{cite";
        String figureStart = "{{";
        String figureEnd = "}}";
        int i = 0;
        while (s.contains(citesStart)) {
            int citeStartIndex = s.indexOf(citesStart);
            i = BracesMatcher.findMatchingBracesIndex(s,figureStart, citeStartIndex);
            s = s.substring(0, citeStartIndex) + s.substring(i + figureEnd.length());
        }
        return s;
    }

    public void setLanguageFooterRemover(LanguageFooterRemover language){
        languageFooterRemover = language;
    }

}
