package org.acme.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.service.CardStatusService;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.smallrye.reactive.messaging.annotations.Blocking;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class CardStatusConsumer {
    private static final Logger logger = Logger.getLogger(CardStatusConsumer.class.getName());

    @Inject
    CardStatusService cardStatusService;

    @Incoming("card-status")
    @Blocking
    @Transactional
    public void processCardStatus(String requestBody) {
        if (requestBody.contains("SetCardStatus")) {
            logger.log(Level.INFO, "Received SetCardStatus request: {0}", requestBody);
            cardStatusService.parseRequestAndSaveToDatabase(requestBody);
        } else if (requestBody.contains("GetCardStatus")) {
            logger.log(Level.INFO, "Received GetCardStatus request: {0}", requestBody);
            cardStatusService.processGetCardStatusRequest(requestBody);
        } else {
            logger.log(Level.WARNING, "Received unknown request: {0}", requestBody);
        }
    }
}
