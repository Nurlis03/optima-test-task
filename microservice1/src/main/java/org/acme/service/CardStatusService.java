package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.acme.dto.CardStatusMethod;
import org.acme.dto.CardStatusParameters;
import org.acme.dto.CardStatusResponse;
import org.acme.redis.RedisCache;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Objects;

import org.jboss.logging.Logger;

@ApplicationScoped
public class CardStatusService {
    private static final Logger log = Logger.getLogger(CardStatusService.class);
    @RestClient
    CardStatusServiceClient cardStatusServiceClient;
    @Inject
    RedisCache redisCache;

    public Response setCardStatus(String requestBody) {
        try {
            Document document = parseXml(requestBody);
            String cardId = extractItem(document, "cardID");
            String newStatus = extractItem(document, "status");

            String cardIdKey = String.format("cardId:%s", cardId);
            String stan = extractAttribute(document, "method", "stan");

            CardStatusMethod method = new CardStatusMethod("SetCardStatus", stan, null);
            CardStatusResponse cardStatusResponse = new CardStatusResponse(1, "SUCCESS", method);

            if (redisCache.get(cardIdKey) == null || !Objects.equals(redisCache.get(cardIdKey), newStatus)) {
                // cardId is not exists in cache or try to set card status to another new status
                redisCache.setex(cardIdKey, newStatus);
            } else if (Objects.equals(redisCache.get(cardIdKey), newStatus)) {
                // try to set status when status in cache equal with new status
                return Response.ok(cardStatusResponse).build();
            }

            // Forward the request to the service client if no change in status or cache miss
            cardStatusServiceClient.setCardStatus(requestBody); // may change this to getCardStatus(cardId, newStatus, stan)

            return Response.ok(cardStatusResponse).build();
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

    public Response getCardStatus(String requestBody) {
        try {
            Document document = parseXml(requestBody);
            String cardId = extractItem(document, "cardID");

            String cardIdKey = String.format("cardId:%s", cardId);
            String stan = extractAttribute(document, "method", "stan");

            String status = redisCache.get(cardIdKey);

            CardStatusParameters parameters = new CardStatusParameters(status);
            CardStatusMethod method = new CardStatusMethod("GetCardStatus", stan, parameters);
            CardStatusResponse cardStatusResponse = new CardStatusResponse(1, "SUCCESS", method);

            if (status != null) {
                log.infof("Retrieved card status from Redis cache. Key: %s, Value: %s", cardIdKey, status);
                return Response.ok(cardStatusResponse).build();
            } else {
                // if status is not exist in redis cache
                String cardResponse = cardStatusServiceClient.getCardStatus(requestBody); // may change this to getCardStatus(cardId, stan)
                if (!cardResponse.contains("<result>0</result>")) {
                    // If the card is found, save it to the cache with cardId
                    document = parseXml(cardResponse);
                    status = extractItem(document, "status");
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
        }
        catch (Exception e) {
            log.error("Failed to process request: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to process request: " + e.getMessage())
                    .build();
        }
    }

    public Document parseXml(String xmlString) throws Exception {
        try (StringReader stringReader = new StringReader(xmlString)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(stringReader));
        }
    }

    public String extractItem(Document document, String tagName) {
        Element element = (Element) document.getElementsByTagName(tagName).item(0);
        if (element == null) {
            log.error("Bad request: Tag '" + tagName + "' not found in request");
            throw new BadRequestException("Tag '" + tagName + "' not found in request");
        }
        log.infof("Received request to process GetCardStatus. %s: %s", tagName, element.getTextContent());
        return element.getTextContent();
    }

    public String extractAttribute(Document document, String tagName, String attributeName) {
        Element element = (Element) document.getElementsByTagName(tagName).item(0);
        return element.getAttribute(attributeName);
    }
}
