package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.producer.CardStatusProducer;
import org.acme.service.CardStatusService;

import java.util.logging.Logger;

@Path("/microservice-1")
public class MainResource {

    private static final Logger logger = Logger.getLogger(MainResource.class.getName());
    @Inject
    CardStatusService cardStatusService;

    @Inject
    CardStatusProducer cardStatusProducer;

    @POST
    @Path("/set-card-status")
    public Response setCardStatus(String requestBody) {
        return cardStatusService.setCardStatus(requestBody);
    }

    @GET
    @Path("/get-card-status")
    public Response getCardStatus(String requestBody) {
        return cardStatusService.getCardStatus(requestBody);
    }

    @POST
    @Path("/rabbit-mq/set-card-status")
    public Response asyncSetCardStatus(String requestBody) {
        return cardStatusProducer.asyncSetCardStatus(requestBody);
    }
    @GET
    @Path("/rabbit-mq/get-card-status")
    @Produces(MediaType.TEXT_PLAIN)
    public Response asyncGetCardStatus(String requestBody) {
        return cardStatusProducer.asyncGetCardStatus(requestBody);
    }
}