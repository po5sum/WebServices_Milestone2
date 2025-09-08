package com.musicstore.apigateway.presentationlayer.customers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.apigateway.customers.presentationlayer.CustomerRequestModel;
import com.musicstore.apigateway.customers.presentationlayer.CustomerResponseModel;
import com.musicstore.apigateway.customers.domainclientlayer.ContactMethodPreference;
import com.musicstore.apigateway.customers.domainclientlayer.PhoneNumber;
import com.musicstore.apigateway.customers.domainclientlayer.PhoneType;
import com.musicstore.apigateway.utils.exceptions.InvalidInputException;
import com.musicstore.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.ArrayList;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql({"/data-h2.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CustomersControllerIntegrationTest {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private ObjectMapper mapper = new ObjectMapper();

    private final String GATEWAY_BASE = "/api/v1/customers";
    private final String DOWNSTREAM_BASE = "http://localhost:7001/api/v1/customers";

    private final String VALID_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String NOT_FOUND_ID = "c3540a89-cb47-4c96-888e-ff96708db4d7";
    private final String INVALID_ID = "c3540a89-cb47-4c96-888e-ff96708d";

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void whenGetAllCustomers_thenReturnStubbedList() throws Exception {
        CustomerResponseModel c1 = CustomerResponseModel.builder()
                .customerId("id1").firstName("John").lastName("Doe").build();
        CustomerResponseModel c2 = CustomerResponseModel.builder()
                .customerId("id2").firstName("Jane").lastName("Roe").build();

        mockServer.expect(ExpectedCount.once(), requestTo(DOWNSTREAM_BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(List.of(c1, c2)), MediaType.APPLICATION_JSON));

        webClient.get().uri(GATEWAY_BASE)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponseModel.class)
                .hasSize(2)
                .value(list -> assertEquals("John", list.get(0).getFirstName()));
    }

    @Test
    void whenGetCustomerByValidId_thenReturnCustomer() throws Exception {
        CustomerResponseModel cust = CustomerResponseModel.builder()
                .customerId(VALID_ID).firstName("Alice").lastName("Smith").build();

        mockServer.expect(requestTo(DOWNSTREAM_BASE + "/" + VALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(cust), MediaType.APPLICATION_JSON));

        webClient.get().uri(GATEWAY_BASE + "/" + VALID_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(response -> assertEquals("Alice", response.getFirstName()));
    }

    @Test
    void whenGetCustomerInvalidId_thenReturn422() throws Exception {
        // stub the downstream service to return 422
        mockServer.expect(ExpectedCount.once(),
                        requestTo(DOWNSTREAM_BASE + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid customerId provided: " + INVALID_ID + "\"}"));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid customerId provided: " + INVALID_ID);
    }


    @Test
    void whenGetCustomerNotFound_thenReturn404() throws Exception {
        mockServer.expect(requestTo(DOWNSTREAM_BASE + "/" + NOT_FOUND_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get().uri(GATEWAY_BASE + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenCreateCustomerValid_thenReturn201() throws Exception {
        CustomerRequestModel req = CustomerRequestModel.builder()
                .firstName("Bob").lastName("Builder").emailAddress("bob@build.com")
                .contactMethodPreference(ContactMethodPreference.EMAIL)
                .build();
        CustomerResponseModel created = CustomerResponseModel.builder()
                .customerId("newId").firstName("Bob").lastName("Builder").build();

        mockServer.expect(requestTo(DOWNSTREAM_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(created)));

        webClient.post().uri(GATEWAY_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponseModel.class)
                .value(resp -> assertEquals("Builder", resp.getLastName()));
    }

    @Test
    void whenCreateCustomerDuplicateEmail_thenReturn422() throws Exception {
        CustomerRequestModel dup = CustomerRequestModel.builder()
                .firstName("Dup").lastName("User").emailAddress("exists@domain.com").build();

        mockServer.expect(requestTo(DOWNSTREAM_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"email already exists\"}"));

        webClient.post().uri(GATEWAY_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dup)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").value(msg -> assertTrue(((String)msg).contains("exists")));
    }

    @Test
    void whenUpdateCustomerValid_thenReturn200() throws Exception {
        CustomerRequestModel upd = CustomerRequestModel.builder()
                .firstName("Updated").lastName("Name").build();
        CustomerResponseModel respModel = CustomerResponseModel.builder()
                .customerId(VALID_ID).firstName("Updated").lastName("Name").build();

        mockServer.expect(requestTo(DOWNSTREAM_BASE + "/" + VALID_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(mapper.writeValueAsString(respModel), MediaType.APPLICATION_JSON));

        webClient.put().uri(GATEWAY_BASE + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(upd)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(resp -> assertEquals("Updated", resp.getFirstName()));
    }

    @Test
    void whenUpdateCustomerInvalidId_thenReturn422() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(DOWNSTREAM_BASE + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid customerId provided: " + INVALID_ID + "\"}"));

        CustomerRequestModel upd = CustomerRequestModel.builder().build();
        webClient.put()
                .uri(GATEWAY_BASE + "/" + INVALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(upd)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid customerId provided: " + INVALID_ID);
    }

    @Test
    void whenUpdateCustomerNotFound_thenReturn404() throws Exception {
        CustomerRequestModel upd = CustomerRequestModel.builder().build();
        mockServer.expect(requestTo(DOWNSTREAM_BASE + "/" + NOT_FOUND_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.put().uri(GATEWAY_BASE + "/" + NOT_FOUND_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(upd)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenDeleteCustomerValid_thenReturn204() throws Exception {
        mockServer.expect(requestTo(DOWNSTREAM_BASE + "/" + VALID_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        webClient.delete().uri(GATEWAY_BASE + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void whenDeleteCustomerInvalidId_thenReturn422() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(DOWNSTREAM_BASE + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid customerId provided: " + INVALID_ID + "\"}"));

        webClient.delete()
                .uri(GATEWAY_BASE + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid customerId provided: " + INVALID_ID);
    }

    @Test
    void whenDeleteCustomerNotFound_thenReturn404() throws Exception {
        mockServer.expect(requestTo(DOWNSTREAM_BASE + "/" + NOT_FOUND_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.delete().uri(GATEWAY_BASE + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound();
    }
/*
    @Test
    public void testCustomerIdentifierConstructorAndGetter() {
        CustomerIdentifier id = new CustomerIdentifier("test-1234");
        assertEquals("test-1234", id.getCustomerId());
    }

 */

    @Test
    public void testExceptionConstructors() {
        // InvalidInputException tests
        RuntimeException cause = new RuntimeException("boom");
        InvalidInputException i1 = new InvalidInputException();
        assertNull(i1.getMessage());
        assertNull(i1.getCause());

        InvalidInputException i2 = new InvalidInputException("msg");
        assertEquals("msg", i2.getMessage());

        InvalidInputException i3 = new InvalidInputException(cause);
        assertSame(cause, i3.getCause());

        InvalidInputException i4 = new InvalidInputException("m2", cause);
        assertEquals("m2", i4.getMessage());
        assertSame(cause, i4.getCause());

        // NotFoundException tests
        NotFoundException n1 = new NotFoundException();
        assertNull(n1.getMessage());
        assertNull(n1.getCause());

        NotFoundException n2 = new NotFoundException("msg");
        assertEquals("msg", n2.getMessage());

        NotFoundException n3 = new NotFoundException(cause);
        assertSame(cause, n3.getCause());

        NotFoundException n4 = new NotFoundException("m2", cause);
        assertEquals("m2", n4.getMessage());
        assertSame(cause, n4.getCause());
    }
}

