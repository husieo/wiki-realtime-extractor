##### Quick Start

Prerequisites: 
* Maven(mvn)
* JDK 1.8

Before running the extractor, you have to  download the dump you are interested with from http://dumps.wikimedia.org/backup-index.html, pick the latest complete one from <lang>wiki (e.g., itwiki) and choose the pages-articles.xml.bz2 one (e.g., itwiki-20120226-pages-articles.xml.bz2).

Next, download this project and submit the XML dump to process via sh script(outputs to /output folder):

```
   ./parse_xml_dump.sh <xml_dump_path> --language=ENGLISH --clean -o=output
```

As an example, the project already contains a test page:

```
   ./parse_xml_dump.sh documents/xml_test_page.xml --language=ENGLISH --clean -o=output
```

On the first run, this script will create the executable. After the extraction is completed, the output will appear in the /output folder, split into context, links and structure.

Possible command line arguments:
```
  -c, --clean     Clean output files
  -h, --help      Show this help message and exit.
  -l, --language=<language>
                  Xml dump language.
  -o, --output=<outputPath>
                  The NIF output folder
```

Larger xml dump example:
```
   ./parse_xml_dump.sh documents/short_test_100k.xml --language=ENGLISH --clean -o=output
```

###### Custom Configuration
You can add more supported languages by adding them to configuration/language_list.xml. The file already contains examples for a few languages, and you can add by following next pattern:
```
    <language>
        <langName>ENGLISH</langName>
        <categoryName>Category</categoryName>
        <footer>See also</footer>
    </language>
```

This pattern supports multiple footer titles.