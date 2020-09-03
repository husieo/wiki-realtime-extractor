package org.dbpedia.extractor.service.remover.language;

import java.util.List;
import java.util.regex.Pattern;

public class Russian extends LanguageFooterRemover {
  public Russian() {

  }

  @Override
  protected List<Pattern> footerPatterns() {
    return footerPatterns("Примечания", "Ссылки", "Литература", "См. также");
  }

  @Override
  protected List<Pattern> categoryLinkPatterns() {
    return categoryLinkPatterns("Категория");
  }
}
