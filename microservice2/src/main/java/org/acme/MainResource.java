package org.acme;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import org.acme.service.CardStatusService;

@Path("/microservice-2")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
public class MainResource {
    @Inject
    CardStatusService cardStatusService;

    @POST
    @Path("/set-card-status")
    @Transactional
    public Response setCardStatus(String request) {
        return cardStatusService.parseRequestAndSaveToDatabase(request);
    }

    @GET
    @Path("/get-card-status")
    public Response getCardStatus(String request) {
        return cardStatusService.processGetCardStatusRequest(request);
    }
}
