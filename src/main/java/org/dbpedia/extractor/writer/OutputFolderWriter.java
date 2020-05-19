package org.dbpedia.extractor.writer;

import java.io.*;
import java.nio.file.Files;

public class OutputFolderWriter {

    public static final String CONTEXT_FILENAME = "nif_context.nt";
    public static final String STRUCTURE_FILENAME = "nif_structure.nt";
    public static final String LINKS_FILENAME = "nif_links.nt";

    private String outputFolder;

    public OutputFolderWriter(String folderName) {
        this.outputFolder = folderName;
        createOutputFolder(folderName);
    }

    private void createOutputFolder(String folderName){
         File folder = new File(folderName);
         if(!folder.exists()){
             try {
                 Files.createDirectory(folder.toPath());
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
    }

    public void writeToFile(String fileName, String text) {
        File fout = new File(outputFolder + "/" + fileName);
        BufferedWriter bw = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fout);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.append(text);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
