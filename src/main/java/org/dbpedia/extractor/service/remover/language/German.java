package org.dbpedia.extractor.service.remover.language;

import java.util.List;
import java.util.regex.Pattern;

public class German extends LanguageFooterRemover {

  public German() {
  }


  @Override
  protected List<Pattern> footerPatterns() {
    return footerPatterns("Referenzen", "Weblinks", "Literatur", "Einzelnachweise", "Siehe auch", "Quellen");
  }

  @Override
  protected List<Pattern> categoryLinkPatterns() {
    return categoryLinkPatterns("Kategorie");
  }
}
