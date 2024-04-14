package org.acme.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.acme.dto.CardStatusResponse;
import org.acme.service.CardStatusService;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.smallrye.reactive.messaging.annotations.Blocking;

import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class CardStatusConsumer {
    private static final Logger logger = Logger.getLogger(CardStatusConsumer.class.getName());

    @Inject
    CardStatusService cardStatusService;

    @Inject
    @Channel("response-channel")
    Emitter<String> sendResponse;

    @Incoming("card-status")
    @Blocking
    @Transactional
    public void processCardStatus(String requestBody) {
        if (requestBody.contains("SetCardStatus")) {
            logger.log(Level.INFO, "Received SetCardStatus request: {0}", requestBody);
            Response response = cardStatusService.parseRequestAndSaveToDatabase(requestBody);
            sendResponse.send(response.getEntity().toString());
        } else if (requestBody.contains("GetCardStatus")) {
            logger.log(Level.INFO, "Received GetCardStatus request: {0}", requestBody);
            Response response = cardStatusService.processGetCardStatusRequest(requestBody);

            // Assuming response contains CardStatusResponse object
            CardStatusResponse cardStatusResponse = (CardStatusResponse) response.getEntity();

            // Send XML response through the emitter
            String str_response = cardStatusResponseToXml(cardStatusResponse);
            logger.info(str_response);
            sendResponse.send(str_response);
        } else {
            logger.log(Level.WARNING, "Received unknown request: {0}", requestBody);
        }
    }

    // Method to convert CardStatusResponse to XML
    private String cardStatusResponseToXml(CardStatusResponse response) {
        try {
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(CardStatusResponse.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(response, writer);
            return writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
}
