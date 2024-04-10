package org.acme.dto;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "method")
@XmlAccessorType(XmlAccessType.FIELD)
public class CardStatusMethod {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "stan")
    private String stan;

    @XmlElement(name = "parameters")
    private CardStatusParameters parameters;

    public CardStatusMethod() {
    }

    public CardStatusMethod(String name, String stan, CardStatusParameters parameters) {
        this.name = name;
        this.stan = stan;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public CardStatusParameters getParameters() {
        return parameters;
    }

    public void setParameters(CardStatusParameters parameters) {
        this.parameters = parameters;
    }
}
