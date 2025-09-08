package com.musicstore.apigateway.presentationlayer.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.apigateway.orders.presentationlayer.OrdersRequestModel;
import com.musicstore.apigateway.orders.presentationlayer.OrdersResponseModel;
import com.musicstore.apigateway.orders.domainclientlayer.OrdersServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class OrdersControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrdersServiceClient ordersServiceClient;

    private MockRestServiceServer mockServer;
    private ObjectMapper mapper = new ObjectMapper();

    private static final String GATEWAY_BASE = "/api/v1/customers";
    private static final String ORDERS_SERVICE_BASE = "http://localhost:7004/api/v1/customers";

    private static final String VALID_CUSTOMER = "c123";
    private static final String INVALID_CUSTOMER = "invalid-id";
    private static final String NOT_FOUND_CUSTOMER = "c404";
    private static final String VALID_ORDER = "o123";
    private static final String NOT_FOUND_ORDER = "o404";

    private OrdersResponseModel sampleOrder;
    private OrdersRequestModel newOrderRequest;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        sampleOrder = OrdersResponseModel.builder()
                .orderId(VALID_ORDER)
                .customerId(VALID_CUSTOMER)
                .orderPrice(19.99)
                .paymentMethod(null)
                .build();
        newOrderRequest = OrdersRequestModel.builder()
                .artistId("a1").albumId("al1").storeId("s1")
                .orderDate("2025-05-12").orderPrice(29.99)
                .paymentMethod(null).build();
    }

    @Test
    void whenValidCustomer_thenGetAllOrders() throws Exception {
        // stub orders-service
        List<OrdersResponseModel> list = List.of(sampleOrder);
        mockServer.expect(ExpectedCount.once(),
                        requestTo(ORDERS_SERVICE_BASE + "/" + VALID_CUSTOMER + "/orders"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(list), MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + VALID_CUSTOMER + "/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrdersResponseModel.class)
                .hasSize(1)
                .value(orders -> assertEquals(VALID_ORDER, orders.get(0).getOrderId()));

        mockServer.verify();
    }

    @Test
    void whenCustomerNotFound_then404() {
        mockServer.expect(requestTo(ORDERS_SERVICE_BASE + "/" + NOT_FOUND_CUSTOMER + "/orders"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"No orders\"}"));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + NOT_FOUND_CUSTOMER + "/orders")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenValidOrder_thenGetOneOrder() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(ORDERS_SERVICE_BASE + "/" + VALID_CUSTOMER + "/orders/" + VALID_ORDER))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(sampleOrder), MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + VALID_CUSTOMER + "/orders/" + VALID_ORDER)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrdersResponseModel.class)
                .value(o -> assertEquals(VALID_ORDER, o.getOrderId()));
    }

    @Test
    void whenValidCreateOrder_then201() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(ORDERS_SERVICE_BASE + "/" + VALID_CUSTOMER + "/orders"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(sampleOrder)));

        webClient.post()
                .uri(GATEWAY_BASE + "/" + VALID_CUSTOMER + "/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newOrderRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrdersResponseModel.class)
                .value(o -> assertEquals(VALID_ORDER, o.getOrderId()));
    }

    @Test
    void whenValidDeleteOrder_then204() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(ORDERS_SERVICE_BASE + "/" + VALID_CUSTOMER + "/orders/" + VALID_ORDER))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        webClient.delete()
                .uri(GATEWAY_BASE + "/" + VALID_CUSTOMER + "/orders/" + VALID_ORDER)
                .exchange()
                .expectStatus().isNoContent();
    }

    // --- update ---
    @Test
    void whenValidUpdateOrder_thenReturnOk() throws Exception {
        // arrange
        var req = OrdersRequestModel.builder()
                .artistId("a1")
                .albumId("al1")
                .storeId("s1")
                .orderDate("2025-05-12")
                .orderPrice(39.99)
                .paymentMethod(null)
                .build();

        var updated = OrdersResponseModel.builder()
                .orderId(VALID_ORDER)
                .customerId(VALID_CUSTOMER)
                .orderPrice(39.99)
                .paymentMethod(null)
                .build();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(ORDERS_SERVICE_BASE + "/" + VALID_CUSTOMER + "/orders/" + VALID_ORDER))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json(mapper.writeValueAsString(req)))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(updated),
                        MediaType.APPLICATION_JSON
                ));

        // act & assert
        webClient.put()
                .uri(GATEWAY_BASE + "/" + VALID_CUSTOMER + "/orders/" + VALID_ORDER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrdersResponseModel.class)
                .value(o -> {
                    assertEquals(VALID_ORDER, o.getOrderId());
                    assertEquals(39.99, o.getOrderPrice());
                });

        mockServer.verify();
    }

    @Test
    void whenUpdateOrderNotFound_thenReturn404() {
        // downstream returns 404
        mockServer.expect(requestTo(ORDERS_SERVICE_BASE + "/" + VALID_CUSTOMER + "/orders/" + NOT_FOUND_ORDER))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Order not found\"}"));

        webClient.put()
                .uri(GATEWAY_BASE + "/" + VALID_CUSTOMER + "/orders/" + NOT_FOUND_ORDER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newOrderRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Order not found");

        mockServer.verify();
    }

    @Test
    void whenUpdateOrderInvalidCustomerOrOrder_thenReturn422() {
        // invalid customerId path
        mockServer.expect(requestTo(ORDERS_SERVICE_BASE + "/" + INVALID_CUSTOMER + "/orders/" + VALID_ORDER))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid UUID\"}"));

        webClient.put()
                .uri(GATEWAY_BASE + "/" + INVALID_CUSTOMER + "/orders/" + VALID_ORDER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newOrderRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid UUID");

        mockServer.verify();
    }
}

