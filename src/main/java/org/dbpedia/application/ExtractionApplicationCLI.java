package org.dbpedia.application;

import org.dbpedia.cli.XmlInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication(scanBasePackages={"org.dbpedia"})
public class ExtractionApplicationCLI implements CommandLineRunner, ExitCodeGenerator {

    private CommandLine.IFactory factory;    // auto-configured to inject PicocliSpringFactory
    private XmlInput myCommand; // your @picocli.CommandLine.Command-annotated class
    private int exitCode;

    public ExtractionApplicationCLI(CommandLine.IFactory factory, XmlInput myCommand) {
        this.factory = factory;
        this.myCommand = myCommand;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(ExtractionApplicationCLI.class, args)));
    }

    @Override
    public void run(String... args) {
        // let picocli parse command line args and run the business logic
        exitCode = new CommandLine(myCommand, factory).execute(args);
    }
}
