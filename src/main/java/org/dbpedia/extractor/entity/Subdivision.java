package org.dbpedia.extractor.entity;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Log4j
public class Subdivision {

    @NonNull
    private int order;

    @NonNull
    private Position position;

    @NonNull
    private String title;

    private List<Subdivision> children = new ArrayList<>();

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
}
