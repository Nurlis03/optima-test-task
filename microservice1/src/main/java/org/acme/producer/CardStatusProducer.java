package org.acme.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class CardStatusProducer {

    private static final Logger logger = Logger.getLogger(CardStatusProducer.class.getName());

    @Inject
    @Channel("card-status")
    Emitter<String> cardStatusRequestEmitter;

    public Response asyncSetCardStatus(String requestBody) {
        if (isValidRequest(requestBody, "SetCardStatus")) {
            logger.log(Level.WARNING, "Invalid SetCardStatus request");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid SetCardStatus request")
                    .build();
        }
        logger.log(Level.INFO, "Sending set card status request to RabbitMQ");
        cardStatusRequestEmitter.send(requestBody);
        return Response.ok("send set card status request to rabbit mq. \n " +
                        "Microservice 2 consume this message and handle it")
                .build();
    }

    public Response asyncGetCardStatus(String requestBody) {
        if (isValidRequest(requestBody, "GetCardStatus")) {
            logger.log(Level.WARNING, "Invalid GetCardStatus request");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid GetCardStatus request")
                    .build();
        }
        logger.log(Level.INFO, "Sending get card status xml request to RabbitMQ");
        cardStatusRequestEmitter.send(requestBody);
        return Response.ok("send get card status xml request to rabbit mq. \n " +
                        "Microservice 2 consume this message and handle it")
                .build();
    }

    private boolean isValidRequest(String requestBody, String methodType) {
        try {
            Document document = parseXml(requestBody);
            Element methodElement = (Element) document.getElementsByTagName("method").item(0);
            String name = methodElement.getAttribute("name");
            return !methodType.equals(name);
        } catch (Exception e) {
            return true;
        }
    }

    private Document parseXml(String xmlString) throws Exception {
        try (StringReader stringReader = new StringReader(xmlString)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new org.xml.sax.InputSource(stringReader));
        }
    }
}
