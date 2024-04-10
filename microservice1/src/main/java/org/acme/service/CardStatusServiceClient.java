package org.acme.service;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// TODO: check api best practice
@Path("/microservice-2") // specify base path to microservice 2: http://localhost:9090/microservice-2
@RegisterRestClient(configKey = "microservice2-api")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface CardStatusServiceClient {
    @POST
    @Path("/set-card-status") // add path for set card status: http://localhost:9090/microservice-2/set-card-status
    String setCardStatus(String requestBody);

    @GET
    @Path("/get-card-status") // add path for get card status: http://localhost:9090/microservice-2/get-card-status
    String getCardStatus(String requestBody);
}
