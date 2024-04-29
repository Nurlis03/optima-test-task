package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.acme.dto.CardStatusMethod;
import org.acme.dto.CardStatusParameters;
import org.acme.dto.CardStatusResponse;
import org.acme.producer.CardStatusProducer;
import org.acme.redis.RedisCache;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.acme.parser.CardStatusParser;
import org.w3c.dom.Document;

import java.util.Objects;

import org.jboss.logging.Logger;

@ApplicationScoped
public class CardStatusService {
    private static final Logger log = Logger.getLogger(CardStatusService.class);
    @RestClient
    CardStatusServiceClient cardStatusServiceClient;
    @Inject
    RedisCache redisCache;

    @Inject
    CardStatusParser parser;

    @Inject
    CardStatusProducer producer;

    public Response setCardStatus(String requestBody, String typeRequest) {
        try {
            Document document = parser.parseXml(requestBody);
            String cardId = parser.extractItem(document, "cardID", "SetCardStatus");
            String newStatus = parser.extractItem(document, "status", "SetCardStatus");
            String cardIdKey = String.format("cardId:%s", cardId);
            String stan = parser.extractAttribute(document, "method", "stan");

            CardStatusMethod method = new CardStatusMethod("SetCardStatus", stan, null);
            CardStatusResponse cardStatusResponse = new CardStatusResponse(1, "SUCCESS", method);

            if (redisCache.get(cardIdKey) == null || !Objects.equals(redisCache.get(cardIdKey), newStatus)) {
                // cardId is not exists in cache or try to set card status to another new status
                redisCache.setex(cardIdKey, newStatus);
            } else if (Objects.equals(redisCache.get(cardIdKey), newStatus)) {
                // try to set status when status in cache equal with new status
                return Response.ok(cardStatusResponse).build();
            }

            if (typeRequest.equals("sync")) {
                // Forward the request to the service client if no change in status or cache miss
                cardStatusServiceClient.setCardStatus(requestBody); // may change this to getCardStatus(cardId, newStatus, stan)
            }
            else if (typeRequest.equals("async")) {
                if (parser.isValidRequest(document, "SetCardStatus")) {
                    producer.asyncSetCardStatus(requestBody);
                }
                else {
                    throw new BadRequestException("The name of the \"SetCardStatus\" method is missing. Set the method name");
                }
            }

            return Response.ok(cardStatusResponse).build();
        } catch (BadRequestException e) {
            log.error("Bad request: " + e.getMessage(), e);
            CardStatusResponse badResponse = new CardStatusResponse(0, e.getMessage(), null);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(badResponse)
                    .build();
        } catch (Exception e) {
            log.error("Failed to process request: " + e.getMessage(), e);
            CardStatusResponse errorResponse = new CardStatusResponse(0, "Failed to process request: " + e.getMessage(), null);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }

    public Response getCardStatus(String requestBody, String typeRequest) {
        try {
            Document document = parser.parseXml(requestBody); // Используем метод parseXml из парсера
            String cardId = parser.extractItem(document, "cardID", "GetCardStatus"); // Используем метод extractItem из парсера
            String cardIdKey = String.format("cardId:%s", cardId);
            String stan = parser.extractAttribute(document, "method", "stan"); // Используем метод extractAttribute из парсера

            String status = redisCache.get(cardIdKey);

            CardStatusParameters parameters = new CardStatusParameters(status);
            CardStatusMethod method = new CardStatusMethod("GetCardStatus", stan, parameters);
            CardStatusResponse cardStatusResponse = new CardStatusResponse(1, "SUCCESS", method);

            if (status != null) {
                log.infof("Retrieved card status from Redis cache. Key: %s, Value: %s", cardIdKey, status);
                return Response.ok(cardStatusResponse).build();
            } else {
                // if status is not exist in redis cache
                String cardResponse = "";
                if (typeRequest.equals("sync")) {
                    // Forward the request to the service client if no change in status or cache miss
                    cardResponse = cardStatusServiceClient.getCardStatus(requestBody); // may change this to getCardStatus(cardId, newStatus, stan)
                }
                else if (typeRequest.equals("async")) {
                    if (parser.isValidRequest(document, "GetCardStatus")) {
                        cardResponse = producer.asyncGetCardStatus(requestBody);
                    }
                    else {
                        throw new BadRequestException("The name of the \"GetCardStatus\" method is missing. Set the method name");
                    }
                }

                log.info(cardResponse);
                if (!cardResponse.contains("<result>0</result>")) {
                    // If the card is found, save it to the cache with cardId
                    document = parser.parseXml(cardResponse);
                    status = parser.extractItem(document, "status", "GetCardStatus");
                    redisCache.setex(cardIdKey, status);
                    log.infof("Saved card status to Redis cache. Key: '%s', Value: '%s'", cardIdKey, status);

                    parameters.setStatus(status);
                    return Response.ok(cardStatusResponse).build();
                }
                cardStatusResponse.setResult(0);
                cardStatusResponse.setDescription("Card not found");
                cardStatusResponse.setMethod(null);
                return Response.ok(cardStatusResponse).build();
            }
        } catch (BadRequestException e) {
            log.error("Bad request: " + e.getMessage(), e);
            CardStatusResponse badResponse = new CardStatusResponse(0, e.getMessage(), null);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(badResponse)
                    .build();
        } catch (Exception e) {
            log.error("Failed to process request: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to process request: " + e.getMessage())
                    .build();
        }
    }
}
