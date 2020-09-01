package org.dbpedia.extractor.service.transformer;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ContextLanguageTransformer implements XmlTransformer {
    private static Map<String, String> languagesMap;

    public ContextLanguageTransformer() {
        // initialize language Map
        languagesMap = new HashMap<>();
        languagesMap.put("fr", "French");
        languagesMap.put("de", "German");
        languagesMap.put("la", "Latin");
        languagesMap.put("grc", "Greek");
        languagesMap.put("grc-gre", "Greek"); // might be Modern Greek
        languagesMap.put("ru", "Russian");
        languagesMap.put("rus", "Russian"); // also Russian
        languagesMap.put("en", "English");
        languagesMap.put("ar", "Arabic");
        languagesMap.put("arq", "Arabic"); // don't know which dialect
        languagesMap.put("ca", "Catalan");
        languagesMap.put("ale", "Aleut");
        languagesMap.put("ik", "Inupiaq");
    }

    public String transformText(String text) {
        String languageStartPattern = "{{lang-";
        int langStartLen = languageStartPattern.length();
        String figureStart = "{{";
        int figureLength = figureStart.length();
        String figureEnd = "}}";
        int i = 0;
        while (text.contains(languageStartPattern)) {
            int langStartIndex = text.indexOf(languageStartPattern);
            int parenthesesCounter = 1;
            i = langStartIndex;
            while (parenthesesCounter > 0) {
                i++;
                String testSubStr = text.substring(i, i + figureLength);
                if (testSubStr.equals(figureStart)) {
                    parenthesesCounter++;
                } else if (testSubStr.equals(figureEnd)) {
                    parenthesesCounter--;
                }
            }
            String entryText = text.substring(langStartIndex + langStartLen, i);
            String extractedText = getLangEntryText(entryText);
            String language = getLanguage(entryText);
            String combinedLangEntry = String.format("%s: %s", language, extractedText);
            text = text.substring(0, langStartIndex) + combinedLangEntry + text.substring(i + 2);
        }
        return text;
    }

    private String getLangEntryText(String entryText) {
        String[] linkArray = entryText.split("\\|");
        entryText = String.format("%s", linkArray[linkArray.length - 1]);
        return entryText;
    }

    private String getLanguage(String entryText) {
        String[] linkArray = entryText.split("\\|");
        String language = linkArray[0];
        return languagesMap.get(language);
    }
}
