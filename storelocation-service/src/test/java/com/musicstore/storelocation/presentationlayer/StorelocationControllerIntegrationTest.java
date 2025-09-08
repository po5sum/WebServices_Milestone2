package com.musicstore.storelocation.presentationlayer;

import com.musicstore.storelocation.dataaccesslayer.StoreIdentifier;
import com.musicstore.storelocation.dataaccesslayer.StoreRepository;
import com.musicstore.storelocation.utils.exceptions.DuplicateAddressException;
import com.musicstore.storelocation.utils.exceptions.InvalidInputException;
import com.musicstore.storelocation.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data-h2.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StorelocationControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private StoreRepository storeRepository;

    private final String BASE_URL_STORES = "api/v1/stores";
    private final String VALID_STORE_ID = "b2d3a4e7-f29b-4f5e-bf1c-8a77a7319a1e";
    private final String INVALID_STORE_ID = "invalid-store-id";
    private final String NOT_FOUND_STORE_ID = "b2d3a4e7-f29b-4f5e-bf1c-8a77a7319a1f";

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
    public void whenStoreRequestIsValid_thenReturnCreatedStore() {
        StoreRequestModel req = sampleRequest();

        webClient.post().uri("/" + BASE_URL_STORES)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
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
    }

    @Test
    public void whenStoreWithDuplicateStreetAddressIsCreated_thenReturnUnprocessableEntity() {
        StoreRequestModel req = sampleRequest();

        // First creation should succeed
        webClient.post().uri("/" + BASE_URL_STORES)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated();

        // Second creation with same streetAddress should fail
        webClient.post().uri("/" + BASE_URL_STORES)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody().jsonPath("$.message")
                .isEqualTo("Store with the same street address already exists.");
    }

    @Test
    public void whenStoreExists_thenReturnAllStores() {
        // arrange: count seeded stores
        long count = storeRepository.count();

        // act + assert
        webClient.get().uri("/" + BASE_URL_STORES)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(StoreResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(count, list.size());
                });
    }

    @Test
    public void whenGetByValidId_thenReturnStore() {
        webClient.get().uri("/" + BASE_URL_STORES + "/" + VALID_STORE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(StoreResponseModel.class)
                .value(store -> assertEquals(VALID_STORE_ID, store.getStoreId()));
    }

    @Test
    public void whenStoreIdIsInvalidOnGet_thenReturnUnprocessableEntity() {
        webClient.get().uri("/" + BASE_URL_STORES + "/" + INVALID_STORE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid storeId provided: " + INVALID_STORE_ID);
    }

    @Test
    public void whenStoreDoesNotExistOnGet_thenReturnNotFound() {
        webClient.get().uri("/" + BASE_URL_STORES + "/" + NOT_FOUND_STORE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Store not found" + NOT_FOUND_STORE_ID);
    }

    @Test
    public void whenStoreExistsOnUpdate_thenReturnUpdatedStore() {
        StoreRequestModel update = sampleRequest().builder()
                .ownerName("AliceUpdated")
                .streetAddress("7007 rue du Troubadour")
                .build();

        webClient.put().uri("/" + BASE_URL_STORES + "/" + VALID_STORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(StoreResponseModel.class)
                .value(store -> {
                    assertEquals("AliceUpdated", store.getOwnerName());
                    assertEquals("7007 rue du Troubadour", store.getStreetAddress());
                });
    }

    @Test
    public void whenStoreIdIsInvalidOnUpdate_thenReturnUnprocessableEntity() {
        webClient.put().uri("/" + BASE_URL_STORES + "/" + INVALID_STORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleRequest())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid storeId provided: " + INVALID_STORE_ID);
    }

    @Test
    public void whenStoreDoesNotExistOnUpdate_thenReturnNotFound() {
        webClient.put().uri("/" + BASE_URL_STORES + "/" + NOT_FOUND_STORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleRequest())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Store not found" + NOT_FOUND_STORE_ID);
    }

    @Test
    public void whenStoreExistsOnDelete_thenReturnNoContent() {
        webClient.delete().uri("/" + BASE_URL_STORES + "/" + VALID_STORE_ID)
                .exchange()
                .expectStatus().isNoContent();

        webClient.get().uri("/" + BASE_URL_STORES + "/" + VALID_STORE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenStoreIdIsInvalidOnDelete_thenReturnUnprocessableEntity() {
        webClient.delete().uri("/" + BASE_URL_STORES + "/" + INVALID_STORE_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid storeId provided: " + INVALID_STORE_ID);
    }

    @Test
    public void whenStoreDoesNotExistOnDelete_thenReturnNotFound() {
        webClient.delete().uri("/" + BASE_URL_STORES + "/" + NOT_FOUND_STORE_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Store not found" + NOT_FOUND_STORE_ID);
    }

    // Exception and Identifier constructor tests

    @Test
    public void testStoreIdentifierConstructorAndGetter() {
        String uuid = UUID.randomUUID().toString();
        StoreIdentifier id = new StoreIdentifier(uuid);
        assertEquals(uuid, id.getStoreId());
    }

    @Test
    public void testDuplicateAddressExceptionConstructors() {
        DuplicateAddressException ex1 = new DuplicateAddressException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());
        DuplicateAddressException ex2 = new DuplicateAddressException("dup");
        assertEquals("dup", ex2.getMessage());
        RuntimeException cause = new RuntimeException("boom");
        DuplicateAddressException ex3 = new DuplicateAddressException(cause);
        assertSame(cause, ex3.getCause());
        DuplicateAddressException ex4 = new DuplicateAddressException("m", cause);
        assertEquals("m", ex4.getMessage());
        assertSame(cause, ex4.getCause());
    }

    @Test
    public void testInvalidInputExceptionConstructors() {
        // no-arg
        InvalidInputException ex1 = new InvalidInputException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        // message-only
        String msg = "Invalid input";
        InvalidInputException ex2 = new InvalidInputException(msg);
        assertEquals(msg, ex2.getMessage());

        // cause-only
        RuntimeException cause = new RuntimeException("cause");
        InvalidInputException ex3 = new InvalidInputException(cause);
        assertSame(cause, ex3.getCause());

        // message + cause
        String msg2 = "Invalid input with cause";
        RuntimeException cause2 = new RuntimeException("cause2");
        InvalidInputException ex4 = new InvalidInputException(msg2, cause2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(cause2, ex4.getCause());
    }

    @Test
    public void testNotFoundExceptionConstructors() {
        // no-arg
        NotFoundException ex1 = new NotFoundException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        // message-only
        String msg = "Not found";
        NotFoundException ex2 = new NotFoundException(msg);
        assertEquals(msg, ex2.getMessage());

        // cause-only
        RuntimeException cause = new RuntimeException("cause");
        NotFoundException ex3 = new NotFoundException(cause);
        assertSame(cause, ex3.getCause());

        // message + cause
        String msg2 = "Not found with cause";
        RuntimeException cause2 = new RuntimeException("cause2");
        NotFoundException ex4 = new NotFoundException(msg2, cause2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(cause2, ex4.getCause());
    }
}
