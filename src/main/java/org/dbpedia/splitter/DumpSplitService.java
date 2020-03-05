package org.dbpedia.splitter;

import lombok.extern.log4j.Log4j;
import org.dbpedia.extractor.entity.WikiPage;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j
public class DumpSplitService {

    private static final String PAGE_ELEM = "page";

    List<WikiPage> textList = new ArrayList<>();

    public void splitPages(String filePath) throws ParserConfigurationException, IOException, SAXException {
        File xmlFile = new File(filePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = factory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

        NodeList nList = doc.getElementsByTagName(PAGE_ELEM);

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) nNode;

                String title = elem.getElementsByTagName("title").item(0).getTextContent();

                Element revisionElement = (Element) elem.getElementsByTagName("revision").item(0);
                Element textElement = (Element) revisionElement.getElementsByTagName("text").item(0);

                String text = textElement.getTextContent();
                textList.add(new WikiPage(title,text));
                log.info(String.format("Page id: %s%n", title));
                log.info(String.format("Text: %s%n", text));
            }
        }
    }
}
