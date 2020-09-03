package org.dbpedia.extractor.service.remover.language;

import java.util.List;
import java.util.regex.Pattern;

public class Italian extends LanguageFooterRemover {
  public Italian(){
  }

  @Override
  protected List<Pattern> footerPatterns() {
    return footerPatterns("Note", "Voci correlate", "Altri progetti", "Collegamenti esterni");
  }

  @Override
  protected List<Pattern> categoryLinkPatterns() {
    return categoryLinkPatterns("Categoria");
  }
}
