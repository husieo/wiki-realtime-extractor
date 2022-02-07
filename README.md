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

Examples for other languages:

```
   ./parse_xml_dump.sh documents/xml_test_polish.xml --language=POLISH --clean -o=output
   ./parse_xml_dump.sh documents/xml_test_deutsch.xml --language=GERMAN --clean -o=output
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

Dumps can be obtained at https://dumps.wikimedia.org/wikidatawiki/.

###### Custom Configuration
You can add more supported languages by adding them to configuration/language_list.xml. The file already contains examples for a few languages, and you can add by following next pattern:
```
    <language>
        <langName>ENGLISH</langName>
        <categoryName>Category</categoryName>
        <footer>See also</footer>
        <isoLangCode>eng</isoLangCode>
    </language>
```
* langName - Language identifier, i.e. ENGLISH/GERMAN/POLISH etc.
* categoryName - Wikipedia identifier for article's categories. In the XML page, is located at the bottom of the page. Examples: \[\[**Category**:Anarchism| ]], [[**Kategorie**:Pseudonym]]
* footer - Wikipedia page section that is not part of the Context. This usually includes next sections: "See also", "References", "Further reading", "External Links", "Related pages".
    Those section titles have to be translated to a new language and verified by visiting that language's Wikipedia pages.
* isoLangCode - ISO 3-letter language code, can be found in https://iso639-3.sil.org/code_tables/639/data

This pattern supports multiple footer titles.

###### Output validation

Output validation can be done via rapper tool(output to file in output_examples folder): 

```
    rapper --input ntriples --output rdfxml output/nif\_context.nt  >> output\_examples/rapper_context.rdf
    rapper --input ntriples --output rdfxml output/nif\_links.nt >> output\_examples/rapper_links.rdf
    rapper --input ntriples --output rdfxml output/nif\_structure.nt >> output\_examples/rapper_structure.rdf
```

SHACL shapes are also defined in \shacl folder, and tested via https://shacl.org/playground/
SHACL properties are defined in https://www.w3.org/TR/shacl/.
To convert N-Triples, use https://www.easyrdf.org/converter/.