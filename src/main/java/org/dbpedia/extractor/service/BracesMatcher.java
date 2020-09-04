package org.dbpedia.extractor.service;

import lombok.Getter;
import lombok.Setter;
import org.dbpedia.exception.ParsingException;

import java.util.HashMap;
import java.util.Map;

public class BracesMatcher {

    @Getter
    @Setter
    public static Map<String, String> matchingBraces;

    public BracesMatcher() {
        //initialize braces map
        matchingBraces = new HashMap<>();
        matchingBraces.put("{{", "}}");
        matchingBraces.put("<!--", "-->");
        matchingBraces.put("<ref>", "</ref>");
        matchingBraces.put("{|", "|}");
        matchingBraces.put("[[","]]");
    }

    /**
     * Find end of an xml component in braces
     *
     * @param text        XML text
     * @param bracesStart brace start
     * @param bracesPos   brace position
     * @return index of the brace end
     */
    public static int findMatchingBracesIndex(String text, String bracesStart, int bracesPos) throws ParsingException {
        String bracesEnd = matchingBraces.get(bracesStart);
        int bracesStartLen = bracesStart.length();
        int bracesEndLen = bracesEnd.length();
        int parenthesesCounter = 1;
        int i = bracesPos;
        while (parenthesesCounter > 0) {
            i++;
            if (i + bracesStartLen < text.length()) {
                String testSubStrStart = text.substring(i, i + bracesStartLen);
                if (testSubStrStart.equals(bracesStart)) {
                    parenthesesCounter++;
                }
            }
            if((i+bracesEndLen) > text.length()){
                throw new ParsingException("Broken xml component");
            }
            String testSubStrEnd = text.substring(i, i + bracesEndLen);
            if (testSubStrEnd.equals(bracesEnd)) {
                parenthesesCounter--;
            }
        }
        return i;
    }
}