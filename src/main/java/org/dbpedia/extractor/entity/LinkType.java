package org.dbpedia.extractor.entity;

public enum LinkType {
    PHRASE("phrase"), WORD("word");
    String typeLabel;
    LinkType(String typeLabel){
        this.typeLabel = typeLabel;
    }
    public String getTypeLabel(){
        return typeLabel;
    }
    public String getCapitalizedTypeLabel(){
        return typeLabel.substring(0,1).toUpperCase() + typeLabel.substring(1);
    }
}
