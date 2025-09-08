package com.musicstore.orders.presentationlayer;


import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.orders.OrdersServiceApplication;
import com.musicstore.orders.dataaccesslayer.Order;
import com.musicstore.orders.dataaccesslayer.OrderRepository;
import com.musicstore.orders.dataaccesslayer.PaymentMethod;
import com.musicstore.orders.domainclientlayer.customer.CustomerModel;
import com.musicstore.orders.domainclientlayer.customer.CustomersServiceClient;
import com.musicstore.orders.domainclientlayer.musiccatalog.AlbumModel;
import com.musicstore.orders.domainclientlayer.musiccatalog.MusicCatalogServiceClient;
import com.musicstore.orders.domainclientlayer.musiccatalog.Status;
import com.musicstore.orders.domainclientlayer.storelocation.StoreLocationModel;
import com.musicstore.orders.domainclientlayer.storelocation.StoresServiceClient;
import com.musicstore.orders.utils.exceptions.InvalidInputException;
import com.musicstore.orders.utils.exceptions.InvalidOrderPriceException;
import com.musicstore.orders.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    MusicCatalogServiceClient musicCatalogServiceClient;

    @Autowired
    CustomersServiceClient customersServiceClient;

    @Autowired
    StoresServiceClient storesServiceClient;

    private MockRestServiceServer mockRestServiceServer;

    private ObjectMapper mapper = new ObjectMapper();

    private final String BASE_URI = "/api/v1/customers";
    private final String FOUND_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String NOT_FOUND_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d9";
    private final String INVALID_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff9670";
    private final String BASE_URI_CUSTOMERS = "http://localhost:7001/api/v1/customers";
    private final String BASE_URI_MUSICCATALOG = "http://localhost:7002/api/v1/artists";
    private final String BASE_URI_STORES = "http://localhost:7003/api/v1/stores";
    private final String ARTIST_ID = "e5913a79-9b1e-4516-9ffd-06578e7af261";
    private final String ALBUM_ID = "84c5f33e-8e5d-4eb5-b35d-79272355fa72";

    private OrderRequestModel orderRequestModel = createOrderRequestModel();

    @BeforeEach
    void init() {
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
        assertTrue(orderRepository.count() > 0, "Database should be pre-loaded with at least one order");
    }

    @Test
    void whenValidCustomerId_thenReturnAllOrders() throws Exception {
        // Arrange
        CustomerModel customerModel = CustomerModel.builder()
                .customerId(FOUND_CUSTOMER_ID)
                .firstName("Alick")
                .lastName("Ucceli")
                .build();

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_CUSTOMERS + "/" + FOUND_CUSTOMER_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(customerModel)));

        List<Order> dbOrders = orderRepository.findAllByCustomerModel_CustomerId(FOUND_CUSTOMER_ID);
        Order existing = dbOrders.get(0);
        String artistId = existing.getAlbumModel().getArtistId();
        String albumId = existing.getAlbumModel().getAlbumId();
        String storeId = existing.getStoreLocationModel().getStoreId();

        AlbumModel albumModel = AlbumModel.builder()
                .artistId(artistId)
                .albumId(albumId)
                .artistName("The Beatles")
                .albumTitle("Abbey Road")
                .status(Status.NEW)
                .build();

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_MUSICCATALOG + "/" + artistId + "/albums/" + albumId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(albumModel)));

        StoreLocationModel storeModel = StoreLocationModel.builder()
                .storeId(storeId)
                .ownerName("John Doe")
                .managerName("Jane Smith")
                .build();

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_STORES + "/" + storeId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(storeModel)));

        // Act & Assert
        webClient.get()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                });
    }

    @Test
    void whenCustomerIdNotFound_thenReturnNotFoundForGetAll() throws Exception {
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_CUSTOMERS + "/" + NOT_FOUND_CUSTOMER_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get()
                .uri(BASE_URI + "/" + NOT_FOUND_CUSTOMER_ID + "/orders")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenInvalidCustomerIdLength_thenReturnUnprocessableEntityForGetAll() {
        webClient.get()
                .uri(BASE_URI + "/" + INVALID_CUSTOMER_ID + "/orders")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenValidCustomerAndOrderId_thenReturnOrder() throws Exception {
        // Arrange
        CustomerModel customerModel = CustomerModel.builder()
                .customerId(FOUND_CUSTOMER_ID)
                .firstName("Alick")
                .lastName("Ucceli")
                .build();

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_CUSTOMERS + "/" + FOUND_CUSTOMER_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(customerModel)));

        // Fetch existing order data
        Order existing = orderRepository.findAllByCustomerModel_CustomerId(FOUND_CUSTOMER_ID).get(0);
        String orderId = existing.getOrderIdentifier().getOrderId();
        String artistId = existing.getAlbumModel().getArtistId();
        String albumId = existing.getAlbumModel().getAlbumId();
        String storeId = existing.getStoreLocationModel().getStoreId();

        AlbumModel albumModel = AlbumModel.builder()
                .artistId(artistId)
                .albumId(albumId)
                .artistName("The Beatles")
                .albumTitle("Abbey Road")
                .status(Status.NEW)
                .build();

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_MUSICCATALOG + "/" + artistId + "/albums/" + albumId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(albumModel)));

        StoreLocationModel storeModel = StoreLocationModel.builder()
                .storeId(storeId)
                .ownerName("John Doe")
                .managerName("Jane Smith")
                .build();

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_STORES + "/" + storeId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(storeModel)));

        // Act & Assert
        webClient.get()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseModel.class)
                .value(response -> {
                    assertNotNull(response.getOrderId());
                    assertEquals(orderId, response.getOrderId());
                    assertEquals("Abbey Road", response.getAlbumTitle());
                    assertEquals("The Beatles", response.getArtistName());
                    assertEquals(PaymentMethod.CASH, response.getPaymentMethod());
                });
    }

    @Test
    void whenOrderIdNotFound_thenReturnBadRequestForGetOne() {
        String unknownOrderId = "00000000-0000-0000-0000-000000000000";
        webClient.get()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders/" + unknownOrderId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenInvalidOrderIdLength_thenReturnUnprocessableForGetOne() {
        webClient.get()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders/" + INVALID_CUSTOMER_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenValidCreateOrder_thenReturnCreated() throws Exception {
        // Arrange
        CustomerModel customerModel = CustomerModel.builder()
                .customerId("dd1ab8b0-ab17-4e03-b70a-84caa3871606")
                .firstName("Ricky")
                .lastName("Presslie")
                .build();

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_CUSTOMERS + "/" + customerModel.getCustomerId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(customerModel)));

        AlbumModel albumModel = AlbumModel.builder()
                .artistId(orderRequestModel.getArtistId())
                .albumId(orderRequestModel.getAlbumId())
                .artistName("Artist X")
                .albumTitle("Album Y")
                .status(Status.NEW)
                .build();

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_MUSICCATALOG + "/" + orderRequestModel.getArtistId() + "/albums/" + orderRequestModel.getAlbumId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(albumModel)));

        StoreLocationModel storeModel = StoreLocationModel.builder()
                .storeId(orderRequestModel.getStoreId())
                .ownerName("Owner Z")
                .managerName("Manager W")
                .build();

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_STORES + "/" + orderRequestModel.getStoreId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(storeModel)));

        webClient.post()
                .uri(BASE_URI + "/" + customerModel.getCustomerId() + "/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderRequestModel)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseModel.class)
                .value(response -> {
                    assertNotNull(response.getOrderId());
                    assertEquals(customerModel.getCustomerId(), response.getCustomerId());
                    assertEquals(orderRequestModel.getOrderPrice(), response.getOrderPrice());
                });
    }

    @Test
    void whenUnknownCustomer_thenReturnNotFoundForCreate() throws Exception {
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_CUSTOMERS + "/" + NOT_FOUND_CUSTOMER_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.post()
                .uri(BASE_URI + "/" + NOT_FOUND_CUSTOMER_ID + "/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderRequestModel)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenInvalidOrderPrice_thenReturnBadRequestForCreate() throws Exception {
        CustomerModel customerModel = CustomerModel.builder()
                .customerId(FOUND_CUSTOMER_ID)
                .firstName("Alick")
                .lastName("Ucceli")
                .build();
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_CUSTOMERS + "/" + FOUND_CUSTOMER_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(customerModel)));

        AlbumModel albumModel = AlbumModel.builder()
                .artistId(orderRequestModel.getArtistId())
                .albumId(orderRequestModel.getAlbumId())
                .artistName("Artist X")
                .albumTitle("Album Y")
                .status(Status.NEW)
                .build();
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_MUSICCATALOG + "/" + orderRequestModel.getArtistId() + "/albums/" + orderRequestModel.getAlbumId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(albumModel)));

        StoreLocationModel storeModel = StoreLocationModel.builder()
                .storeId(orderRequestModel.getStoreId())
                .ownerName("Owner Z")
                .managerName("Manager W")
                .build();
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_STORES + "/" + orderRequestModel.getStoreId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(storeModel)));

        // INVALID PRICE
        OrderRequestModel invalid = OrderRequestModel.builder()
                .artistId(orderRequestModel.getArtistId())
                .albumId(orderRequestModel.getAlbumId())
                .storeId(orderRequestModel.getStoreId())
                .orderPrice(0.0)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        webClient.post()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenValidUpdateOrder_thenReturnUpdatedOrder() throws Exception {
        // pick an existing order
        Order existing = orderRepository
                .findAllByCustomerModel_CustomerId(FOUND_CUSTOMER_ID).get(0);
        String orderId = existing.getOrderIdentifier().getOrderId();

        // === STUB customer lookup ===
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_CUSTOMERS + "/" + FOUND_CUSTOMER_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(
                                CustomerModel.builder()
                                        .customerId(FOUND_CUSTOMER_ID)
                                        .firstName("Foo")
                                        .lastName("Bar")
                                        .build()
                        )));

        // === STUB album lookup ===
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_MUSICCATALOG + "/"
                                + existing.getAlbumModel().getArtistId()
                                + "/albums/"
                                + existing.getAlbumModel().getAlbumId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(
                                AlbumModel.builder()
                                        .artistId(existing.getAlbumModel().getArtistId())
                                        .albumId(existing.getAlbumModel().getAlbumId())
                                        .artistName(existing.getAlbumModel().getArtistName())
                                        .albumTitle(existing.getAlbumModel().getAlbumTitle())
                                        .status(existing.getAlbumModel().getStatus())
                                        .build()
                        )));

        // === STUB store lookup ===
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_STORES + "/"
                                + existing.getStoreLocationModel().getStoreId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(
                                StoreLocationModel.builder()
                                        .storeId(existing.getStoreLocationModel().getStoreId())
                                        .ownerName(existing.getStoreLocationModel().getOwnerName())
                                        .managerName(existing.getStoreLocationModel().getManagerName())
                                        .build()
                        )));

        // now perform the PUT
        webClient.put()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderRequestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseModel.class)
                .value(resp -> assertEquals(orderId, resp.getOrderId()));
    }


    @Test
    void whenInvalidCustomerIdOrOrderIdForUpdate_thenReturnUnprocessableEntity() {
        webClient.put()
                .uri(BASE_URI + "/" + INVALID_CUSTOMER_ID + "/orders/" + INVALID_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenValidDelete_thenReturnOk() throws Exception {
        // Fetch existing order
        Order existing = orderRepository.findAllByCustomerModel_CustomerId(FOUND_CUSTOMER_ID).get(0);
        String orderId = existing.getOrderIdentifier().getOrderId();

        webClient.delete()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Order deleted successfully");
    }

    @Test
    void whenInvalidCustomerIdOrOrderIdForDelete_thenReturnUnprocessableEntity() {
        webClient.delete()
                .uri(BASE_URI + "/" + INVALID_CUSTOMER_ID + "/orders/" + INVALID_CUSTOMER_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenOrderNotFound_thenReturnBadRequestForDelete() {
        String unknownOrderId = "00000000-0000-0000-0000-000000000000";
        webClient.delete()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders/" + unknownOrderId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // Helper
    private OrderRequestModel createOrderRequestModel() {
        return OrderRequestModel.builder()
                .artistId("e5913a79-9b1e-4516-9ffd-06578e7af261")
                .albumId("84c5f33e-8e5d-4eb5-b35d-79272355fa72")
                .storeId("b2d3a4e7-f29b-4f5e-bf1c-8a77a7319a1e")
                .orderPrice(29.99)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();
    }

    // --- PUT (update) positive path ---
    @Test
    void whenValidUpdateOrder_thenReturnOk() throws Exception {
        // Arrange: stub customer lookup
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_CUSTOMERS + "/" + FOUND_CUSTOMER_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(
                                CustomerModel.builder()
                                        .customerId(FOUND_CUSTOMER_ID)
                                        .firstName("Foo")
                                        .lastName("Bar")
                                        .build()
                        )));

        // pick an existing order
        Order existing = orderRepository
                .findAllByCustomerModel_CustomerId(FOUND_CUSTOMER_ID).get(0);
        String orderId = existing.getOrderIdentifier().getOrderId();

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_MUSICCATALOG + "/" + existing.getAlbumModel().getArtistId()
                                + "/albums/" + existing.getAlbumModel().getAlbumId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(
                                AlbumModel.builder()
                                        .artistId(existing.getAlbumModel().getArtistId())
                                        .albumId(existing.getAlbumModel().getAlbumId())
                                        .artistName("Updated Artist")
                                        .albumTitle("Updated Title")
                                        .status(Status.NEW)
                                        .build()
                        )));

        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_STORES + "/" + existing.getStoreLocationModel().getStoreId())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(
                                StoreLocationModel.builder()
                                        .storeId(existing.getStoreLocationModel().getStoreId())
                                        .ownerName("Owner X")
                                        .managerName("Mgr Y")
                                        .build()
                        )));

        OrderRequestModel update = OrderRequestModel.builder()
                .artistId(existing.getAlbumModel().getArtistId())
                .albumId(existing.getAlbumModel().getAlbumId())
                .storeId(existing.getStoreLocationModel().getStoreId())
                .orderPrice(55.55)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        // Act & Assert
        webClient.put()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseModel.class)
                .value(resp -> {
                    assertEquals(orderId, resp.getOrderId());
                    assertEquals(55.55, resp.getOrderPrice());
                    assertEquals(PaymentMethod.CASH, resp.getPaymentMethod());
                    assertEquals("Updated Title", resp.getAlbumTitle());
                });
    }

    // --- PUT negative: unknown order id ---
    @Test
    void whenUnknownOrder_thenReturnUnprocessableEntityForUpdate() {
        webClient.put()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders/00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // --- PUT negative: invalid payload (e.g. zero price) ---
    @Test
    void whenInvalidOrderPrice_thenReturnUnprocessableEntityForUpdate() throws Exception {
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_CUSTOMERS + "/" + FOUND_CUSTOMER_ID)))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(
                                CustomerModel.builder().customerId(FOUND_CUSTOMER_ID).firstName("X").lastName("Y").build()
                        )));
        // album
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_MUSICCATALOG + "/" + orderRequestModel.getArtistId()
                                + "/albums/" + orderRequestModel.getAlbumId())))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(
                                AlbumModel.builder()
                                        .artistId(orderRequestModel.getArtistId())
                                        .albumId(orderRequestModel.getAlbumId())
                                        .artistName("A").albumTitle("B").status(Status.NEW).build()
                        )));
        // store
        mockRestServiceServer.expect(ExpectedCount.once(),
                        requestTo(new URI(BASE_URI_STORES + "/" + orderRequestModel.getStoreId())))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(
                                StoreLocationModel.builder()
                                        .storeId(orderRequestModel.getStoreId())
                                        .ownerName("O").managerName("M").build()
                        )));

        OrderRequestModel bad = OrderRequestModel.builder()
                .artistId(orderRequestModel.getArtistId())
                .albumId(orderRequestModel.getAlbumId())
                .storeId(orderRequestModel.getStoreId())
                .orderPrice(0.0)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        webClient.put()
                .uri(BASE_URI + "/" + FOUND_CUSTOMER_ID + "/orders/"
                        + orderRepository.findAllByCustomerModel_CustomerId(FOUND_CUSTOMER_ID).get(0)
                        .getOrderIdentifier().getOrderId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bad)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void tesInvalidOderPriceException() {
        // no-arg
        InvalidOrderPriceException ex1 = new InvalidOrderPriceException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        // message-only
        String msg = "dup email";
        InvalidOrderPriceException ex2 = new InvalidOrderPriceException(msg);
        assertEquals(msg, ex2.getMessage());

        // cause-only
        RuntimeException cause = new RuntimeException("boom");
        InvalidOrderPriceException ex3 = new InvalidOrderPriceException(cause);
        assertSame(cause, ex3.getCause());

        // message + cause
        String msg2 = "dup2";
        RuntimeException cause2 = new RuntimeException("kaboom");
        InvalidOrderPriceException ex4 = new InvalidOrderPriceException(msg2, cause2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(cause2, ex4.getCause());
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

    // --- MusicCatalog Service Client ---
    @Test
    void getArtistByArtistId_returnsAlbumModel() {
        // Arrange
        AlbumModel expected = AlbumModel.builder()
                .artistId(ARTIST_ID)
                .artistName("The Who")
                .albumId(null)
                .albumTitle(null)
                .status(Status.NEW)
                .build();

        mockRestServiceServer.expect(requestTo(BASE_URI_MUSICCATALOG + "/" + ARTIST_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"artistId\":\"" + ARTIST_ID + "\",\"artistName\":\"The Who\"}",
                        MediaType.APPLICATION_JSON));

        // Act
        AlbumModel result = musicCatalogServiceClient.getArtistByArtistId(ARTIST_ID);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getArtistId(),   result.getArtistId());
        assertEquals(expected.getArtistName(), result.getArtistName());
    }

    @Test
    void getAlbumByAlbumId_parsesJsonAndDefaultsCondition() {
        // Arrange: return JSON with lowercase conditionType and missing artistName
        String json = """
        {
          "artistId":"%s",
          "albumId":"%s",
          "albumTitle":"Ziggy Stardust",
          "conditionType":"bargain"
        }
        """.formatted(ARTIST_ID, ALBUM_ID);

        mockRestServiceServer.expect(requestTo(
                        BASE_URI_MUSICCATALOG + "/" + ARTIST_ID + "/albums/" + ALBUM_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        // Act
        AlbumModel album = musicCatalogServiceClient.getAlbumByAlbumId(ARTIST_ID, ALBUM_ID);

        // Assert
        assertEquals(ARTIST_ID,             album.getArtistId());
        assertEquals(ALBUM_ID,              album.getAlbumId());
        assertEquals("Ziggy Stardust",      album.getAlbumTitle());
        assertEquals(Status.BARGAIN,        album.getStatus());
        // changed: JsonNode.asText() yields "" for missing fields
        assertTrue(album.getArtistName().isEmpty(), "artistName should be empty string when missing in payload");
    }

    @Test
    void patchAlbumConditionType_updatesAndReparses() {
        // Arrange: stub the PATCH endpoint to echo back new JSON
        String responseJson = """
            {
              "artistId":"%s",
              "albumId":"%s",
              "artistName":"Queen",
              "albumTitle":"Greatest Hits",
              "conditionType":"used"
            }
            """.formatted(ARTIST_ID, ALBUM_ID);

        mockRestServiceServer.expect(requestTo(
                        BASE_URI_MUSICCATALOG + "/" + ARTIST_ID + "/albums/" + ALBUM_ID + "/condition"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // Act
        AlbumModel updated = musicCatalogServiceClient
                .patchAlbumConditionTypeByArtistAndAlbumId(ARTIST_ID, ALBUM_ID, Status.USED);

        // Assert
        assertEquals(Status.USED, updated.getStatus());
        assertEquals("Queen",    updated.getArtistName());
    }

    @Test
    void errorHandling_404_and_422_translateToCustomExceptions() {
        // 404 => NotFoundException
        mockRestServiceServer.expect(requestTo(BASE_URI_MUSICCATALOG + "/" + ARTIST_ID))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"no artist\"}"));

        assertThrows(NotFoundException.class,
                () -> musicCatalogServiceClient.getArtistByArtistId(ARTIST_ID));

        mockRestServiceServer.reset();

        // 422 => InvalidInputException
        mockRestServiceServer.expect(requestTo(
                        BASE_URI_MUSICCATALOG + "/" + ARTIST_ID + "/albums/" + ALBUM_ID))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"bad album id\"}"));

        assertThrows(InvalidInputException.class,
                () -> musicCatalogServiceClient.getAlbumByAlbumId(ARTIST_ID, ALBUM_ID));
    }

    // ==== CUSTOMERS-SERVICE CLIENT TESTS ====
    @Test
    void getCustomerByCustomerId_success() throws Exception {
        // Arrange
        String custId = "test-cust";
        CustomerModel expected = CustomerModel.builder()
                .customerId(custId)
                .firstName("First")
                .lastName("Last")
                .build();

        mockRestServiceServer.expect(requestTo(BASE_URI_CUSTOMERS + "/" + custId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        // Act
        CustomerModel actual = customersServiceClient.getCustomerByCustomerId(custId);

        // Assert
        assertNotNull(actual);
        assertEquals(expected.getCustomerId(), actual.getCustomerId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
    }

    @Test
    void getCustomerByCustomerId_notFound_throwsNotFoundException() {
        // Arrange
        String custId = "no-cust";
        String errorJson = String.format(
                "{\"message\":\"Customer not found\",\"path\":\"/api/v1/customers/%s\",\"timestamp\":\"2025-05-12T12:00:00Z\"}",
                custId);

        mockRestServiceServer.expect(requestTo(BASE_URI_CUSTOMERS + "/" + custId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson));

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> customersServiceClient.getCustomerByCustomerId(custId));
        assertEquals("Customer not found", ex.getMessage());
    }

    @Test
    void getCustomerByCustomerId_unprocessable_throwsInvalidInputException() {
        // Arrange
        String custId = "bad-cust";
        String errorJson = String.format(
                "{\"message\":\"Invalid customerId\",\"path\":\"/api/v1/customers/%s\",\"timestamp\":\"2025-05-12T12:05:00Z\"}",
                custId);

        mockRestServiceServer.expect(requestTo(BASE_URI_CUSTOMERS + "/" + custId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson));

        // Act & Assert
        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> customersServiceClient.getCustomerByCustomerId(custId));
        assertEquals("Invalid customerId", ex.getMessage());
    }

    @Test
    void getCustomerByCustomerId_malformedErrorJson_returnsFallbackMessage() {
        // Arrange: 404 but non-JSON body
        String custId = "broken-cust";
        mockRestServiceServer.expect(requestTo(BASE_URI_CUSTOMERS + "/" + custId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("not-a-json"));

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> customersServiceClient.getCustomerByCustomerId(custId));
        assertNotNull(ex.getMessage());
    }

    // ==== STORES-SERVICE CLIENT TESTS ====
    @Test
    void getStoreByStoreId_success() throws Exception {
        // Arrange
        String storeId = "s1";
        StoreLocationModel expected = StoreLocationModel.builder()
                .storeId(storeId)
                .ownerName("Owner")
                .managerName("Manager")
                .build();

        mockRestServiceServer.expect(requestTo(BASE_URI_STORES + "/" + storeId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        // Act
        StoreLocationModel actual = storesServiceClient.getStoreByStoreId(storeId);

        // Assert
        assertNotNull(actual);
        assertEquals(expected.getStoreId(), actual.getStoreId());
        assertEquals(expected.getOwnerName(), actual.getOwnerName());
        assertEquals(expected.getManagerName(), actual.getManagerName());
    }

    @Test
    void getStoreByStoreId_notFound_throwsNotFoundException() {
        // Arrange
        String storeId = "no-store";
        String errorJson = String.format(
                "{\"message\":\"Store not found\",\"path\":\"/api/v1/stores/%s\",\"timestamp\":\"2025-05-12T15:00:00Z\"}",
                storeId);

        mockRestServiceServer.expect(requestTo(BASE_URI_STORES + "/" + storeId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson));

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> storesServiceClient.getStoreByStoreId(storeId));
        assertEquals("Store not found", ex.getMessage());
    }

    @Test
    void getStoreByStoreId_unprocessable_throwsInvalidInputException() {
        // Arrange
        String storeId = "bad-store";
        String errorJson = String.format(
                "{\"message\":\"Invalid storeId\",\"path\":\"/api/v1/stores/%s\",\"timestamp\":\"2025-05-12T15:05:00Z\"}",
                storeId);

        mockRestServiceServer.expect(requestTo(BASE_URI_STORES + "/" + storeId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson));

        // Act & Assert
        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> storesServiceClient.getStoreByStoreId(storeId));
        assertEquals("Invalid storeId", ex.getMessage());
    }

    @Test
    void getStoreByStoreId_malformedErrorJson_returnsFallbackMessage() {
        // Arrange: 404 but non-JSON body
        String storeId = "broken-store";
        mockRestServiceServer.expect(requestTo(BASE_URI_STORES + "/" + storeId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("not-a-json"));

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> storesServiceClient.getStoreByStoreId(storeId));
        assertNotNull(ex.getMessage());
    }
}