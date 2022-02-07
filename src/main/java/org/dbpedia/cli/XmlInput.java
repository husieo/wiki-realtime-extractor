package org.dbpedia.cli;

import org.dbpedia.extractor.service.XmlDumpService;
import org.dbpedia.extractor.service.remover.language.LanguageIdentifierBean;
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
    private XmlDumpService xmlDumpService;

    @Autowired
    private LanguageIdentifierBean languageIdentifierBean;

    @CommandLine.Parameters(index = "0", description = "The XML Wiki dump")
    private File xmlFile;

    @CommandLine.Option(names = {"-o", "--output"}, defaultValue = "/output"
            , description = "The NIF output folder")
    private File outputPath;

    @CommandLine.Option(names = {"-c","--clean"}, description = "Clean output files")
    private boolean cleanFiles;

    @CommandLine.Option(names = {"-l","--language"}, defaultValue = "ENGLISH",
            description = "Xml dump language. " +
                    "Valid values are ENGLISH, CHINESE, FRENCH, GERMAN, ITALIAN, RUSSIAN, SPANISH, or any language added to configuration/language_list.xml")
    private String language;

    @CommandLine.Option(names = {"-d","--dry-run"}, defaultValue = "false",
            description = "Whether to output NIF triples. Used for performance testing. Removes NIF file contentc on every iteration if set to true.")
    private boolean dryRun = false;

    public Integer call() throws Exception {
        //check if needed to remove file content
        if(cleanFiles){
            OutputFolderWriter folderWriter = new OutputFolderWriter(outputPath.getPath());
            folderWriter.removeAllFileContents();
        }

        // business logic here
        languageIdentifierBean.readLanguageList();
        languageIdentifierBean.setLanguage(language);
        xmlDumpService.submitXml(xmlFile.getPath(), outputPath.getPath(),dryRun);
        return 0;
    }


}
