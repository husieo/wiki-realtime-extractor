package org.dbpedia.extractor.service.remover.language;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.dbpedia.extractor.entity.xml.Mediawiki;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("singleton")
public class LanguageIdentifierBean {
    private String language;

    private Map<String, DynamicLanguage> languageMap = new HashMap<String, DynamicLanguage>();

    public DynamicLanguage getLanguage() {
        return languageMap.get(language);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void readLanguageList(){
        XmlMapper xmlMapper = new XmlMapper();
        String langConfigRelativePath = "configuration/language_list.xml";
        try {
            String content = Files.readString(Paths.get(langConfigRelativePath), StandardCharsets.UTF_8);
            LanguageContainer languageContainer = xmlMapper.readValue(content, LanguageContainer.class);
            setLanguageMap(languageContainer.getLanguages());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLanguageMap(List<DynamicLanguage> languageList){
        for ( DynamicLanguage dynamicLanguage: languageList) {
            languageMap.put(dynamicLanguage.getLangName(), dynamicLanguage);
        }
    }
}
