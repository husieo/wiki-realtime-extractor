package org.dbpedia.cli;

import org.dbpedia.extractor.service.XmlDumpService;
import org.dbpedia.extractor.writer.OutputFolderWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "submitXml", mixinStandardHelpOptions = true, description = "Submit the XML file for parsing. Returns the output in a specified with -o folder.")
public class XmlInput implements Callable<Integer> {

    @Autowired
    XmlDumpService xmlDumpService;

    @CommandLine.Parameters(index = "0", description = "The XML Wiki dump")
    private File xmlFile;

    @CommandLine.Option(names = {"-o", "--output"}, description = "The NIF output folder")
    private File outputPath;

    @CommandLine.Option(names = {"-c","--clean"}, description = "Clean output files")
    private boolean cleanFiles;

    public Integer call() throws Exception {
        //check if needed to remove file content
        if(cleanFiles){
            OutputFolderWriter folderWriter = new OutputFolderWriter(outputPath.getPath());
            folderWriter.removeAllFileContents();
        }

        // business logic here
        xmlDumpService.submitXml(xmlFile.getPath(), outputPath.getPath());
        return 0;
    }
}
