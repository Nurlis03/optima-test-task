package org.acme.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.parser.CardStatusParser;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class CardStatusProducer {

    private static final Logger logger = Logger.getLogger(CardStatusProducer.class.getName());

    private CompletableFuture<String> responseFuture = new CompletableFuture<String>();

    @Inject
    private CardStatusParser parser;

    @Inject
    @Channel("card-status")
    Emitter<String> cardStatusRequestEmitter;

    public void asyncSetCardStatus(String requestBody) {
        logger.log(Level.INFO, "Sending set card status request to RabbitMQ");
        cardStatusRequestEmitter.send(requestBody);
    }

    public String asyncGetCardStatus(String requestBody) {

        responseFuture = new CompletableFuture<>();

        logger.log(Level.INFO, "Sending get card status xml request to RabbitMQ");
        cardStatusRequestEmitter.send(requestBody);

        try {
            // We are waiting for a response from microservice 2
            String response = responseFuture.get();

            return response;
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Failed to get response from microservice 2", e);
            return e.getMessage();
        }
    }

    @Incoming("response-channel")
    public void getResponse(String response) {
        // Completing CompletableFuture with the received response
        responseFuture.completeAsync(() -> response);
    }
}
