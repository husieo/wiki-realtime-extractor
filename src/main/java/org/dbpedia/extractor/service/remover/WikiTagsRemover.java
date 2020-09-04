package org.dbpedia.extractor.service.remover;

import org.dbpedia.exception.ParsingException;
import org.dbpedia.extractor.service.BracesMatcher;
import org.dbpedia.extractor.service.remover.language.*;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class WikiTagsRemover {

    private LanguageFooterRemover languageFooterRemover;

    private static final Pattern EMPHASIS = Pattern.compile("('''|'')");
    private static final Pattern HTML_COMMENT = Pattern.compile(
            "(<|&lt;|&#60;)!--.*?--(>|&gt;|&#62;)", Pattern.DOTALL);
    private static final Pattern UNIT_CONVERSION1 =
            Pattern.compile("\\{\\{convert\\|(\\d+)\\|([^|]+)\\}\\}");
    private static final Pattern UNIT_CONVERSION2 =
            Pattern.compile("\\{\\{convert\\|(\\d+)\\|([^|]+)\\|[^}]+\\}\\}");
    private static final Pattern GALLERY = Pattern.compile("&lt;gallery&gt;.*?&lt;/gallery&gt;",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern NO_TOC = Pattern.compile("__NOTOC__");
    private static final Pattern INDENTATION = Pattern.compile("[\\n\\r]:\\s*");
    private static final Pattern MATH = Pattern.compile("&lt;math&gt;.*?&lt;/math&gt;",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // IPA parenthetical may be enclosed either with parentheses or brackets (de articles).
    private static final Pattern IPA1 = Pattern.compile(" (\\(|\\[)\\{\\{IPA[^\\}]+\\}\\}(\\)|\\])");
    private static final Pattern IPA2 = Pattern.compile(" \\{\\{IPA[^\\}]+\\}\\}");
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");

    public String removeMath(String s) {
        return MATH.matcher(s).replaceAll("");
    }

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
        // Note that we shouldn't just leave to the double-curly remover, since that would leave
        // the dangling empty parens.
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
            s = s.substring(0, citeStartIndex) + s.substring(i + figureEnd.length() + 1);
        }
        return s;
    }

    public void setLanguageFooterRemover(WikiLanguages language){
        switch (language){
            case FRENCH:
                languageFooterRemover = new French();
                break;
            case GERMAN:
                languageFooterRemover = new German();
                break;
            case CHINESE:
                languageFooterRemover = new Chinese();
                break;
            case ENGLISH:
                languageFooterRemover = new English();
                break;
            case ITALIAN:
                languageFooterRemover = new Italian();
                break;
            case RUSSIAN:
                languageFooterRemover = new Russian();
                break;
            case SPANISH:
                languageFooterRemover = new Spanish();
                break;
        }
    }

}
