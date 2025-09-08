package com.musicstore.apigateway.presentationlayer.storelocation;

import com.musicstore.apigateway.storelocation.presentationlayer.StoreRequestModel;
import com.musicstore.apigateway.storelocation.presentationlayer.StoreResponseModel;
import com.musicstore.apigateway.storelocation.presentationlayer.StoresController;
import com.musicstore.apigateway.storelocation.domainclientlayer.StoresServiceClient;
import com.musicstore.apigateway.utils.exceptions.InvalidInputException;
import com.musicstore.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ExpectedCount;
import com.fasterxml.jackson.databind.ObjectMapper;


import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StoresControllerIntegrationTest {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StoresServiceClient storesServiceClient;

    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String GATEWAY_BASE = "/api/v1/stores";
    private static final String DOWNSTREAM_BASE = "http://localhost:7003/api/v1/stores";

    private final String VALID_STORE_ID      = "b2d3a4e7-f29b-4f5e-bf1c-8a77a7319a1e";
    private final String INVALID_STORE_ID    = "invalid-store-id";
    private final String NOT_FOUND_STORE_ID  = "b2d3a4e7-f29b-4f5e-bf1c-8a77a7319a1f";

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    private StoreRequestModel sampleRequest() {
        return StoreRequestModel.builder()
                .ownerName("Alice")
                .managerName("Bob")
                .storeRating(4.5)
                .phoneNumber("555-1234")
                .email("alice@example.com")
                .openHours("9-5")
                .streetAddress("11110 Main St")
                .city("Metropolis")
                .province("State")
                .postalCode("A1A1A1")
                .build();
    }

    @Test
    void whenStoreRequestIsValid_thenReturnCreatedStore() throws Exception {
        var req = sampleRequest();
        var resp = StoreResponseModel.builder()
                .storeId(UUID.randomUUID().toString())
                .ownerName(req.getOwnerName())
                .managerName(req.getManagerName())
                .storeRating(req.getStoreRating())
                .phoneNumber(req.getPhoneNumber())
                .email(req.getEmail())
                .openHours(req.getOpenHours())
                .streetAddress(req.getStreetAddress())
                .city(req.getCity())
                .province(req.getProvince())
                .postalCode(req.getPostalCode())
                .build();

        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(resp)));

        webClient.post()
                .uri(GATEWAY_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(StoreResponseModel.class)
                .value(store -> {
                    assertNotNull(store.getStoreId());
                    assertEquals(req.getOwnerName(), store.getOwnerName());
                    assertEquals(req.getStreetAddress(), store.getStreetAddress());
                });

        mockServer.verify();
    }

    @Test
    void whenStoreWithDuplicateStreetAddressIsCreated_thenReturnUnprocessableEntity() throws Exception {
        var req = sampleRequest();

        // register both stubs before any calls
        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(req)));

        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Store with the same street address already exists.\"}"));

        // now make the two calls in order
        webClient.post()
                .uri(GATEWAY_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated();

        webClient.post()
                .uri(GATEWAY_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Store with the same street address already exists.");

        mockServer.verify();
    }


    @Test
    void whenGetAllStores_thenReturnList() throws Exception {
        var list = List.of(
                StoreResponseModel.builder().storeId("id1").build(),
                StoreResponseModel.builder().storeId("id2").build()
        );

        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(list), MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(GATEWAY_BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(StoreResponseModel.class)
                .value(respList -> assertEquals(2, respList.size()));

        mockServer.verify();
    }


    @Test
    void whenGetStoreValid_thenReturnStore() throws Exception {
        var resp = StoreResponseModel.builder().storeId(VALID_STORE_ID).build();

        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE + "/" + VALID_STORE_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(resp), MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + VALID_STORE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(StoreResponseModel.class)
                .value(store -> assertEquals(VALID_STORE_ID, store.getStoreId()));

        mockServer.verify();
    }

    @Test
    void whenStoreIdInvalidOnGet_thenReturn422() throws Exception {
        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE + "/" + INVALID_STORE_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid storeId provided: " + INVALID_STORE_ID + "\"}"));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + INVALID_STORE_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid storeId provided: " + INVALID_STORE_ID);

        mockServer.verify();
    }

    @Test
    void whenStoreDoesNotExistOnGet_thenReturn404() throws Exception {
        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE + "/" + NOT_FOUND_STORE_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Store not found" + NOT_FOUND_STORE_ID + "\"}"));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + NOT_FOUND_STORE_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Store not found" + NOT_FOUND_STORE_ID);

        mockServer.verify();
    }

    @Test
    void whenUpdateStoreValid_thenReturnOk() throws Exception {
        var update = StoreRequestModel.builder()
                .ownerName("AliceUpdated")
                .managerName("Bob")
                .storeRating(4.5)
                .phoneNumber("555-1234")
                .email("alice@example.com")
                .openHours("9-5")
                .streetAddress("7007 rue du Troubadour")
                .city("Metropolis")
                .province("State")
                .postalCode("A1A1A1")
                .build();

        var updated = StoreResponseModel.builder()
                .storeId(VALID_STORE_ID)
                .ownerName(update.getOwnerName())
                .managerName(update.getManagerName())
                .storeRating(update.getStoreRating())
                .phoneNumber(update.getPhoneNumber())
                .email(update.getEmail())
                .openHours(update.getOpenHours())
                .streetAddress(update.getStreetAddress())
                .city(update.getCity())
                .province(update.getProvince())
                .postalCode(update.getPostalCode())
                .build();

        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE + "/" + VALID_STORE_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(mapper.writeValueAsString(updated), MediaType.APPLICATION_JSON));

        webClient.put()
                .uri(GATEWAY_BASE + "/" + VALID_STORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(StoreResponseModel.class)
                .value(store -> {
                    assertEquals("AliceUpdated", store.getOwnerName());
                    assertEquals("7007 rue du Troubadour", store.getStreetAddress());
                });

        mockServer.verify();
    }

    @Test
    void whenStoreIdInvalidOnUpdate_thenReturn422() throws Exception {
        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE + "/" + INVALID_STORE_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid storeId provided: " + INVALID_STORE_ID + "\"}"));

        webClient.put()
                .uri(GATEWAY_BASE + "/" + INVALID_STORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleRequest())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid storeId provided: " + INVALID_STORE_ID);

        mockServer.verify();
    }

    @Test
    void whenStoreDoesNotExistOnUpdate_thenReturn404() throws Exception {
        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE + "/" + NOT_FOUND_STORE_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Store not found" + NOT_FOUND_STORE_ID + "\"}"));

        webClient.put()
                .uri(GATEWAY_BASE + "/" + NOT_FOUND_STORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleRequest())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Store not found" + NOT_FOUND_STORE_ID);

        mockServer.verify();
    }


    @Test
    void whenStoreExistsOnDelete_thenReturnNoContent() throws Exception {
        // 1) stub DELETE
        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE + "/" + VALID_STORE_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        // 2) stub subsequent GET
        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE + "/" + VALID_STORE_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // now do the two calls in sequence
        webClient.delete()
                .uri(GATEWAY_BASE + "/" + VALID_STORE_ID)
                .exchange()
                .expectStatus().isNoContent();

        webClient.get()
                .uri(GATEWAY_BASE + "/" + VALID_STORE_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenStoreIdInvalidOnDelete_thenReturn422() throws Exception {
        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE + "/" + INVALID_STORE_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid storeId provided: " + INVALID_STORE_ID + "\"}"));

        webClient.delete()
                .uri(GATEWAY_BASE + "/" + INVALID_STORE_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid storeId provided: " + INVALID_STORE_ID);

        mockServer.verify();
    }

    @Test
    void whenStoreDoesNotExistOnDelete_thenReturn404() throws Exception {
        mockServer.expect(once(), requestTo(DOWNSTREAM_BASE + "/" + NOT_FOUND_STORE_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Store not found" + NOT_FOUND_STORE_ID + "\"}"));

        webClient.delete()
                .uri(GATEWAY_BASE + "/" + NOT_FOUND_STORE_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Store not found" + NOT_FOUND_STORE_ID);

        mockServer.verify();
    }


    // --- Model and exception constructor tests ---

    @Test
    void testInvalidInputExceptionConstructors() {
        InvalidInputException ex1 = new InvalidInputException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());
        InvalidInputException ex2 = new InvalidInputException("Invalid input");
        assertEquals("Invalid input", ex2.getMessage());
        RuntimeException cause = new RuntimeException("cause");
        InvalidInputException ex3 = new InvalidInputException(cause);
        assertSame(cause, ex3.getCause());
        InvalidInputException ex4 = new InvalidInputException("Invalid input with cause", cause);
        assertEquals("Invalid input with cause", ex4.getMessage());
        assertSame(cause, ex4.getCause());
    }

    @Test
    void testNotFoundExceptionConstructors() {
        NotFoundException ex1 = new NotFoundException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());
        NotFoundException ex2 = new NotFoundException("Not found");
        assertEquals("Not found", ex2.getMessage());
        RuntimeException cause = new RuntimeException("cause");
        NotFoundException ex3 = new NotFoundException(cause);
        assertSame(cause, ex3.getCause());
        NotFoundException ex4 = new NotFoundException("Not found with cause", cause);
        assertEquals("Not found with cause", ex4.getMessage());
        assertSame(cause, ex4.getCause());
    }
}

