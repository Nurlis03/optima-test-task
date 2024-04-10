package org.acme.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class CardStatusResponse {

    @XmlElement(name = "result")
    private int result;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "method")
    private CardStatusMethod method;

    public CardStatusResponse() {
    }

    public CardStatusResponse(int result, String description, CardStatusMethod method) {
        this.result = result;
        this.description = description;
        this.method = method;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CardStatusMethod getMethod() {
        return method;
    }

    public void setMethod(CardStatusMethod method) {
        this.method = method;
    }
}
