package org.acme.parser;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import org.acme.service.CardStatusService;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@ApplicationScoped
public class CardStatusParser {

    private static final Logger log = Logger.getLogger(CardStatusService.class);
    public Document parseXml(String xmlString) throws Exception {
        try (StringReader stringReader = new StringReader(xmlString)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(stringReader));
        }
    }

    public String extractItem(Document document, String tagName, String methodName) {
        Element element = (Element) document.getElementsByTagName(tagName).item(0);
        if (element == null) {
            log.error("Bad request: Tag '" + tagName + "' not found in request");
            throw new BadRequestException("Tag '" + tagName + "' not found in request");
        }
        log.infof("Received request to process %s. %s: %s", methodName, tagName, element.getTextContent());
        return element.getTextContent();
    }

    public String extractAttribute(Document document, String tagName, String attributeName) {
        Element element = (Element) document.getElementsByTagName(tagName).item(0);
        return element.getAttribute(attributeName);
    }

    public boolean isValidRequest(Document document, String methodType) {
        try {
            Element methodElement = (Element) document.getElementsByTagName("method").item(0);
            String name = methodElement.getAttribute("name");
            return methodType.equals(name);
        } catch (Exception e) {
            return true;
        }
    }
}
