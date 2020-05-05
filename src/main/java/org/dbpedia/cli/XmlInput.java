package org.dbpedia.cli;

import org.dbpedia.extractor.service.XmlDumpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@Component
@CommandLine.Command(name = "submitXml", mixinStandardHelpOptions = true, description = "Submit the XML file for parsing. Returns the output in a specified with -o folder.")
public class XmlInput implements Callable<Integer> {

    @Autowired
    XmlDumpService xmlDumpService;

    @CommandLine.Parameters(index = "0", description = "The XML Wiki dump")
    private File xmlFile;

    @CommandLine.Option(names = {"-o", "--output"}, description = "The NIF output folder")
    private File outputPath;

    public Integer call() throws Exception {
        // business logic here
        xmlDumpService.submitXml(readFile(xmlFile));
        writeToFile(outputPath, xmlDumpService.getNifContext());
        return 0;
    }

    private static String readFile(File xmlFile)
    {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( xmlFile.toPath(), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    private static void writeToFile(File outputFile, String text){
        try {
            FileWriter myWriter = new FileWriter(outputFile);
            myWriter.write(text);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
