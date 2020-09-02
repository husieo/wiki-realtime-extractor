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

    private void createOutputFolder(String folderName) {
        File folder = new File(folderName);
        if (!folder.exists()) {
            try {
                Files.createDirectory(folder.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeAllFileContents() {
        removeFileContents(CONTEXT_FILENAME);
        removeFileContents(STRUCTURE_FILENAME);
        removeFileContents(LINKS_FILENAME);
    }

    private void removeFileContents(String filename) {
        try {
            new FileWriter(new File(outputFolder + "/" + filename), false).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String fileName, String text) {
        File fout = new File(outputFolder + "/" + fileName);
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(fout.toPath().toString(), true);
            bw = new BufferedWriter(fw);
            bw.write(text);
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
