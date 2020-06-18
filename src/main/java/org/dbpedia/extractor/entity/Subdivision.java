package org.dbpedia.extractor.entity;

import lombok.*;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Log4j
@ToString
public class Subdivision {

    @NonNull
    private int order;

    @NonNull
    private Position position;

    @NonNull
    private String title;

    private List<Subdivision> children = new ArrayList<>();

    private List<Paragraph> paragraphs = new ArrayList<>();

    public void addChild(Subdivision child){
        children.add(child);
    }

    public void logSubdivisionTree(){
        log.info(traverseSubdivisionTree(this).toString());
    }

    public StringBuilder traverseSubdivisionTree(Subdivision root){
        StringBuilder tree = new StringBuilder();
        tree.append(StringUtils.repeat("--", root.getOrder())).append(root.title);
        for(Subdivision child : root.children){
            tree.append(traverseSubdivisionTree(child));
        }
        return tree;
    }

    public List<Link> getLinks(){
        List<Link> links = new ArrayList<>();
        for(Paragraph paragraph : paragraphs){
            links.addAll(paragraph.getLinks());
        }
        for(Subdivision child : children){
            links.addAll(child.getLinks());
        }
        return links;
    }

    public Position getPosition() {
        return position;
    }
}
