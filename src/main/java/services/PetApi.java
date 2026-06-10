package services;

import dto.PetDto;
import dto.PetResponse;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class PetApi {
    private RequestSpecification spec;
    private String baseUri = System.getProperty("base.uri");
    private String proxyHost = System.getProperty("proxy.host");
    private int proxyPort = Integer.parseInt(System.getProperty("proxy.port"));

    public PetApi() {
        spec = given()
                .relaxedHTTPSValidation() // Отключает проверку SSL для этого запроса
                .proxy(proxyHost, proxyPort) // прокси/VPN клиента
                .baseUri(baseUri)
                .basePath("/v2")
                .contentType(ContentType.JSON);
    }

    public ValidatableResponse createPet(PetDto petDto) {
        return given(spec)
                .body(petDto)
                .log().all()
                .when()
                .post("/pet")
                .then()
                .log().all();
    }

    public ValidatableResponse getPet(String petId) {
        return given(spec)
                .log().all()
                .when()
                .get("/pet/{petId}", petId)
                .then()
                .log().all();
    }

    public ValidatableResponse updatePet(PetResponse petDto) {
        return given(spec)
                .body(petDto)
                .log().all()
                .when()
                .put("/pet")
                .then()
                .log().all();
    }

    public ValidatableResponse findPetsByStatus(String status) {
        return given(spec)
                .queryParam("status", status)
                .log().all()
                .when()
                .get("/pet/findByStatus")
                .then()
                .log().all();
    }
}
