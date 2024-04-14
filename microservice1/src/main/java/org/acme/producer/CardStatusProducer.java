package org.acme.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class CardStatusProducer {

    private static final Logger logger = Logger.getLogger(CardStatusProducer.class.getName());

    private CompletableFuture<String> responseFuture = new CompletableFuture<>();

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

        // Возвращаем ответ о том, что запрос успешно отправлен
        return Response.ok("Set card status request sent successfully").build();
    }

    public Response asyncGetCardStatus(String requestBody) {
        if (isValidRequest(requestBody, "GetCardStatus")) {
            logger.log(Level.WARNING, "Invalid GetCardStatus request");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid GetCardStatus request")
                    .build();
        }

        responseFuture = new CompletableFuture<>();

        logger.log(Level.INFO, "Sending get card status xml request to RabbitMQ");
        cardStatusRequestEmitter.send(requestBody);

        try {
            // Ждем ответа от микросервиса 2
            String response = responseFuture.get();

            logger.info(response);
            return Response.ok(response).build();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Failed to get response from microservice 2", e);
            return Response.serverError().build();
        }
    }

    @Incoming("response-channel")
    public void getResponse(String response) {
        // Завершаем CompletableFuture с полученным ответом
        responseFuture.completeAsync(() -> response);
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
