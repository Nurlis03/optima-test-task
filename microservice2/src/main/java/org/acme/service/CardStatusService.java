package org.acme.service;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.acme.dto.*;
import org.acme.entity.CardStatusEntity;
import org.acme.repository.CardStatusRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import org.jboss.logging.Logger;

@ApplicationScoped
public class CardStatusService {

    private static final Logger log = Logger.getLogger(CardStatusService.class);

    @Inject
    CardStatusRepository cardStatusRepository;

    public Response parseRequestAndSaveToDatabase(String request) {
        try {
            Document document = parseXml(request);
            String cardId = extractItem(document, "cardID");
            String status = extractItem(document, "status");
            String stan = extractAttribute(document, "method", "stan");

            log.infof("Received request to parse XML and save to database. CardID: %s, Status: %s, STAN: %s", cardId, status, stan);

            CardStatusEntity cardStatusEntity = findByCardID(Long.valueOf(cardId));
            if (cardStatusEntity == null) {
                cardStatusEntity = new CardStatusEntity();
                cardStatusEntity.setCardId(Long.valueOf(cardId));
            }

            cardStatusEntity.setStatus(status);
            cardStatusRepository.persist(cardStatusEntity);

            log.info("Saved card status to database.");

            CardStatusMethod cardStatusMethod = new CardStatusMethod("SetCardStatus", stan, null);
            CardStatusResponse cardStatusResponse = new CardStatusResponse(1, "SUCCESS", cardStatusMethod);

            return Response.ok(cardStatusResponse).build();
        } catch (BadRequestException e) {
            CardStatusResponse badResponse = new CardStatusResponse(0, e.getMessage(), null);
            log.error("Bad request: " + e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                                           .entity(badResponse)
                                           .build();
        } catch (Exception e) {
            log.error("Failed to parse request and save to database: " + e.getMessage(), e);
            return Response.serverError()
                           .entity("Failed to parse request and save to database: " + e.getMessage())
                           .build();
        }
    }

    public Response processGetCardStatusRequest(String request) {
        try {
            Document document = parseXml(request);
            String cardId = extractItem(document, "cardID");
            String stan = extractAttribute(document, "method", "stan");

            log.infof("Received request to process GetCardStatus. CardID: %s, STAN: %s", cardId, stan);

            CardStatusEntity cardStatusEntity = findByCardID(Long.valueOf(cardId));
            if (cardStatusEntity != null) {
                CardStatusParameters parameters = new CardStatusParameters(cardStatusEntity.getStatus());
                CardStatusMethod method = new CardStatusMethod("GetCardStatus", stan, parameters);
                CardStatusResponse cardStatusResponse = new CardStatusResponse(1, "SUCCESS", method);
                return Response.ok(cardStatusResponse)
                               .build();
            } else {
                log.warn("Card not found.");
                CardStatusResponse cardNotFoundResponse = new CardStatusResponse(0, "Card not found", null);
                return Response.ok(cardNotFoundResponse)
                               .build();
            }
        } catch (BadRequestException e) {
            CardStatusResponse badResponse = new CardStatusResponse(0, e.getMessage(), null);
            log.error("Bad request: " + e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(badResponse)
                           .build();
        } catch (Exception e) {
            CardStatusResponse errorResponse = new CardStatusResponse(0, "Failed to process request: " + e.getMessage(), null);
            log.error("Failed to process request: " + e.getMessage(), e);
            return Response.serverError()
                    .entity(errorResponse)
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
            String errorMessage = "Tag '" + tagName + "' not found in request";
            log.error("Bad request: " + errorMessage);
            throw new BadRequestException(errorMessage);
        }
        return element.getTextContent();
    }

    public CardStatusEntity findByCardID(Long cardID) {
        return cardStatusRepository.findByCardID(cardID);
    }

    public String extractAttribute(Document document, String tagName, String attributeName) {
        Element element = (Element) document.getElementsByTagName(tagName).item(0);
        return element.getAttribute(attributeName);
    }
}