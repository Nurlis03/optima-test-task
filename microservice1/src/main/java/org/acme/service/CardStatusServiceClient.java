package org.acme.service;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// TODO: check api best practice
@Path("/microservice-2") // specify base path to microservice 2: http://localhost:2222/microservice-2 local profile
@RegisterRestClient(configKey = "microservice2-api")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface CardStatusServiceClient {
    @POST
    @Path("/set-card-status") // add path for set card status: http://localhost:2222/microservice-2/set-card-status local profile
    String setCardStatus(String requestBody);

    @GET
    @Path("/get-card-status") // add path for get card status: http://localhost:2222/microservice-2/get-card-status local profile
    String getCardStatus(String requestBody);
}
