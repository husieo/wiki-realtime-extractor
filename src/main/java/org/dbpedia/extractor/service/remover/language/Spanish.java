package org.dbpedia.extractor.service.remover.language;

import java.util.List;
import java.util.regex.Pattern;

public class Spanish extends LanguageFooterRemover {
  public Spanish() {
  }

  @Override
  protected List<Pattern> footerPatterns() {
    return footerPatterns("Enlaces externos", "Referencias", "Véase también", "Notas");
  }

  @Override
  protected List<Pattern> categoryLinkPatterns() {
    return categoryLinkPatterns("Categoría");
  }
}
