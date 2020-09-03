package org.dbpedia.extractor.service.remover.language;

import java.util.List;
import java.util.regex.Pattern;

public class French extends LanguageFooterRemover {
  public French() {
    super();
  }

  @Override
  protected List<Pattern> footerPatterns() {
    return footerPatterns("Références", "Voir aussi", "Sources", "Annexes", "Articles connexes", "Notes et références", "Liens externes", "Bibliographie", "Source de la traduction", "Source");
  }

  @Override
  protected List<Pattern> categoryLinkPatterns() {
    return categoryLinkPatterns("Catégorie");
  }
}
