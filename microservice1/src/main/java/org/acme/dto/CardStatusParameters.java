package org.acme.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
@XmlAccessorType(XmlAccessType.FIELD)
public class CardStatusParameters {

    @XmlElement(name = "cardID")
    private String cardId;

    @XmlElement(name = "status")
    private String status;

    public CardStatusParameters() {
    }

    public CardStatusParameters(String cardId, String status) {
        this.cardId = cardId;
        this.status = status;
    }

    public CardStatusParameters(String status) {
        this.status = status;
    }

    public String getCardID() {
        return cardId;
    }

    public void setCardID(String cardID) {
        this.cardId = cardID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}