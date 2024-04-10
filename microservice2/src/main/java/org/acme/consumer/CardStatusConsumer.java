package org.acme.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.service.CardStatusService;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.smallrye.reactive.messaging.annotations.Blocking;

@ApplicationScoped
public class CardStatusConsumer {
    @Inject
    CardStatusService cardStatusService;

    @Incoming("card-status")
    @Blocking
    @Transactional
    public void processCardStatus(String requestBody) {
        if (requestBody.contains("SetCardStatus")) {
            cardStatusService.parseRequestAndSaveToDatabase(requestBody);
        } else if (requestBody.contains("GetCardStatus")) {
            cardStatusService.processGetCardStatusRequest(requestBody);
        }
    }
}
