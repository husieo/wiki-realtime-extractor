package org.dbpedia.extractor.service.remover.language;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class LanguageIdentifierBean {
    private WikiLanguages language;

    public WikiLanguages getLanguage() {
        return language;
    }

    public void setLanguage(WikiLanguages language) {
        this.language = language;
    }
}
