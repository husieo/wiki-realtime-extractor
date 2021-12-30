package org.dbpedia.extractor.service.transformer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class ContextLanguageTransformer implements XmlTransformer {
    private static Map<String, String> languagesMap;

    public ContextLanguageTransformer() {
        // initialize language Map
        InputStream inputStream = getClass()
                .getClassLoader().getResourceAsStream("iso_language_codes.csv");
        languagesMap = new HashMap<>();
        try {
            CSVParser parser = CSVParser.parse(new InputStreamReader(inputStream), CSVFormat.RFC4180);
            for (CSVRecord csvRecord : parser) {
                languagesMap.put(csvRecord.get(0),csvRecord.get(1));
            }
        } catch (IOException e) {
            //Missing ISO-639 language codes file
            e.printStackTrace();
        }
    }

    public String transformText(String text) {
        String languageStartPattern = "{{lang-";
        int langStartLen = languageStartPattern.length();
        String figureStart = "{{";
        int figureLength = figureStart.length();
        String figureEnd = "}}";
        int i;
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
        int textIndex = linkArray.length - 1;
        //wikipedia puts pronunication or links last sometimes
        Set<String> lastElementChecks = new HashSet<>();
        lastElementChecks.add("p");
        lastElementChecks.add("link");
        if(linkArray[textIndex].contains("=")){
            String elementPurpose = linkArray[textIndex]
                    .substring(0, linkArray[textIndex].indexOf("="));
            if(lastElementChecks.contains(elementPurpose)){
                textIndex--;
            }
        }
        entryText = String.format("%s", linkArray[textIndex]);
        return entryText;
    }

    private String getLanguage(String entryText) {
        String[] linkArray = entryText.split("\\|");
        String language = linkArray[0];
        String result = "Unknown language";
        if(languagesMap.containsKey(language)) {
            result = languagesMap.get(language);
        }
        return result;
    }
}
