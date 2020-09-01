package org.dbpedia.extractor.service.transformer;

import org.dbpedia.extractor.service.BracesMatcher;
import org.springframework.stereotype.Component;

@Component
public class CitationTransformer implements XmlTransformer {
    @Override
    public String transformText(String text) {
        String citeStartPattern = "{{cite";
        int citeStartLen = citeStartPattern.length();
        String figureStart = "{{";
        int figureLength = figureStart.length();
        String figureEnd = "}}";
        int i = 0;
        while (text.contains(citeStartPattern)) {
            int index = text.indexOf(citeStartPattern);
            int tagEndIndex = BracesMatcher.findMatchingBracesIndex(text, figureStart, index);
            String entryText = text.substring(index + citeStartLen, tagEndIndex);
            String[] Array = entryText.split("\\|");
//            String combinedLangEntry = String.format("%s: %s", language, extractedText);
//            text = text.substring(0, index) + combinedLangEntry;
        }
        return text;
    }

}
