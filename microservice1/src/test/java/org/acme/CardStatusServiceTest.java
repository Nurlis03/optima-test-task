package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import io.restassured.http.ContentType;

@QuarkusTest
class CardStatusServiceTest {

    // setCardStatus method testing from CardStatusService
    @Test
    public void testSetCardStatusEndpoint() {
        String requestBody = "<request>\n" +
                "    <method name=\"SetCardStatus\" stan=\"12345\">\n" +
                "        <parameters>\n" +
                "            <cardID>9999</cardID>\n" +
                "            <status>INACTIVE</status>\n" +
                "        </parameters>\n" +
                "    </method>\n" +
                "</request>";

        given()
                .contentType(ContentType.XML)
                .body(requestBody)
                .when()
                .post("/microservice-1/set-card-status")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("response.result", is("1"))
                .body("response.description", is("SUCCESS"))
                .body("response.method.@stan", is("12345")) // Compare stan attribute
                .body("response.method.@name", is("SetCardStatus")); // Compare name attribute
    }

    @Test
    public void testSetCardStatusWhenStatusNotFound() {
        String requestBody = "<request>\n" +
                "    <method name=\"SetCardStatus\" stan=\"12345\">\n" +
                "        <parameters>\n" +
                "            <cardID>123</cardID>\n" +
                "        </parameters>\n" +
                "    </method>\n" +
                "</request>";

        given()
                .contentType(ContentType.XML)
                .body(requestBody)
                .when()
                .post("/microservice-1/set-card-status")
                .then()
                .statusCode(400) // bad request
                .contentType(ContentType.XML)
                .body("response.result", is("0"))
                .body("response.description", is("Tag 'status' not found in request"));
    }

    @Test
    public void testSetCardStatusWhenCardIDNotFound() {
        String requestBody = "<request>\n" +
                "    <method name=\"SetCardStatus\" stan=\"12345\">\n" +
                "        <parameters>\n" +
                "        </parameters>\n" +
                "    </method>\n" +
                "</request>";

        given()
                .contentType(ContentType.XML)
                .body(requestBody)
                .when()
                .post("/microservice-1/set-card-status")
                .then()
                .statusCode(400) // bad request
                .contentType(ContentType.XML)
                .body("response.result", is("0"))
                .body("response.description", is("Tag 'cardID' not found in request"));
    }

    // -----------------------------------------------------------------------------------------------------------

    // getCardStatus method testing from CardStatusService
    @Test
    public void testSuccessfulGetCardStatus() {
        // Simulate a request for a card status that exists in the database
        String requestBody = "<request>\n" +
                "    <method name=\"GetCardStatus\" stan=\"0\">\n" +
                "        <parameters>\n" +
                "            <cardID>123</cardID>\n" +
                "        </parameters>\n" +
                "    </method>\n" +
                "</request>";

        given()
                .contentType(ContentType.XML)
                .body(requestBody)
                .when()
                .get("/microservice-1/get-card-status")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("response.result", is("1"))
                .body("response.description", is("SUCCESS"))
                .body("response.method.@stan", is("0")) // Compare stan attribute
                .body("response.method.@name", is("GetCardStatus")) // Compare name attribute
                .body("response.method.parameters.status", is("OK"));
    }

    @Test
    public void testCardNotFound() {
        // Simulate a request for a card status that doesn't exist in the external service
        String requestBody = "<request>\n" +
                "    <method name=\"GetCardStatus\" stan=\"0\">\n" +
                "        <parameters>\n" +
                "            <cardID>2984312098</cardID>\n" + // Assuming this card doesn't exist in the external service
                "        </parameters>\n" +
                "    </method>\n" +
                "</request>";

        given()
                .contentType(ContentType.XML)
                .body(requestBody)
                .when()
                .get("/microservice-1/get-card-status")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("response.result", is("0"))
                .body("response.description", is("Card not found"));
    }

    @Test
    public void testBadRequest() {
        // Simulate a bad request when cardID was not transmitted
        String requestBody = "<request>\n" +
                "    <method name=\"GetCardStatus\">\n" +
                "        <parameters>\n" +
                "        </parameters>\n" +
                "    </method>\n" +
                "</request>";

        given()
                .contentType(ContentType.XML)
                .body(requestBody)
                .when()
                .get("/microservice-1/get-card-status")
                .then()
                .statusCode(400) // Bad Request
                .body("response.result", is("0"))
                .body("response.description", is("Tag 'cardID' not found in request"));
    }
}