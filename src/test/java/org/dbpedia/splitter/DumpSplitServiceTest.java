package org.dbpedia.splitter;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

class DumpSplitServiceTest {

        DumpSplitService dumpSplitService = new DumpSplitService();

        @Test
        public void testSplit() throws IOException, SAXException, ParserConfigurationException {

            dumpSplitService.splitPages("/Users/mac/projects/wiki-realtime-extractor/documents/xml_test_page.xml");
        }
}