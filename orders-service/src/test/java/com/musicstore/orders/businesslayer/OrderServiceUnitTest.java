package com.musicstore.orders.businesslayer;

import com.musicstore.orders.dataaccesslayer.Order;
import com.musicstore.orders.dataaccesslayer.OrderIdentifier;
import com.musicstore.orders.dataaccesslayer.OrderRepository;
import com.musicstore.orders.dataaccesslayer.PaymentMethod;
import com.musicstore.orders.domainclientlayer.customer.CustomerModel;
import com.musicstore.orders.domainclientlayer.customer.CustomersServiceClient;
import com.musicstore.orders.domainclientlayer.musiccatalog.AlbumModel;
import com.musicstore.orders.domainclientlayer.musiccatalog.MusicCatalogServiceClient;
import com.musicstore.orders.domainclientlayer.musiccatalog.Status;
import com.musicstore.orders.domainclientlayer.storelocation.StoreLocationModel;
import com.musicstore.orders.domainclientlayer.storelocation.StoresServiceClient;
import com.musicstore.orders.mappinglayer.OrderRequestMapper;
import com.musicstore.orders.mappinglayer.OrderResponseMapper;
import com.musicstore.orders.presentationlayer.OrderRequestModel;
import com.musicstore.orders.presentationlayer.OrderResponseModel;
import com.musicstore.orders.utils.exceptions.InvalidInputException;
import com.musicstore.orders.utils.exceptions.InvalidOrderPriceException;
import com.musicstore.orders.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration")
@ActiveProfiles("test")
public class OrderServiceUnitTest {

    @Autowired
    OrderService orderService;

    @MockitoBean
    OrderRepository orderRepository;

    @MockitoSpyBean
    OrderResponseMapper orderResponseMapper;

    @MockitoBean
    private OrderRequestMapper orderRequestMapper;

    @MockitoBean
    CustomersServiceClient customersServiceClient;

    @MockitoBean
    MusicCatalogServiceClient musicCatalogServiceClient;

    @MockitoBean
    StoresServiceClient storesServiceClient;

    private final String CUST_ID  = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String ORDER_ID = "00000000-0000-0000-0000-000000000001";

    private Order buildOrderEntity() {
        var album = AlbumModel.builder()
                .artistId("a1").albumId("al1")
                .artistName("Artist").albumTitle("Title")
                .status(Status.NEW)
                .build();
        var cust = CustomerModel.builder()
                .customerId(CUST_ID)
                .firstName("First")
                .lastName("Last")
                .build();
        var store = StoreLocationModel.builder()
                .storeId("s1")
                .ownerName("Owner")
                .managerName("Manager")
                .build();
        return Order.builder()
                .orderIdentifier(new OrderIdentifier())
                .albumModel(album)
                .customerModel(cust)
                .storeLocationModel(store)
                .orderDate(LocalDate.now())
                .orderPrice(42.0)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();
    }

    //Get all
    @Test
    public void whenValidCustomerId_thenGetAllOrders() {
        var entity = buildOrderEntity();
        // stub repository & upstream
        when(orderRepository.findAllByCustomerModel_CustomerId(CUST_ID))
                .thenReturn(List.of(entity));
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(entity.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId("a1", "al1"))
                .thenReturn(entity.getAlbumModel());
        when(storesServiceClient.getStoreByStoreId("s1"))
                .thenReturn(entity.getStoreLocationModel());
        // stub response mapper
        var dummyResp = new OrderResponseModel();
        when(orderResponseMapper.entityListToResponseModelList(any()))
                .thenReturn(List.of(dummyResp));

        var result = orderService.getAllOrdersByCustomerId(CUST_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderResponseMapper, times(1))
                .entityListToResponseModelList(List.of(entity));
    }

    @Test
    public void whenCustomerNotFound_thenThrowNotFoundOnGetAll() {
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenThrow(new NotFoundException("no cust"));
        assertThrows(NotFoundException.class,
                () -> orderService.getAllOrdersByCustomerId(CUST_ID));
    }

    @Test
    public void whenOrderNotFound_thenThrowInvalidInputOnGetOne() {
        when(orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(CUST_ID, ORDER_ID))
                .thenReturn(null);
        assertThrows(InvalidInputException.class,
                () -> orderService.findOrderBydOrderId(CUST_ID, ORDER_ID));
    }

    @Test
    public void whenInvalidOrderId_thenThrowInvalidInputOnGetOne() {
        assertThrows(InvalidInputException.class,
                () -> orderService.findOrderBydOrderId(CUST_ID, "short"));
    }

    //create
    @Test
    public void whenValidCreate_thenReturnCreated() {
        var entity = buildOrderEntity();
        var req = OrderRequestModel.builder()
                .artistId("a1")
                .albumId("al1")
                .storeId("s1")
                .orderPrice(100.0)
                .paymentMethod(PaymentMethod.CASH)
                .build();
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(entity.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId("a1","al1"))
                .thenReturn(entity.getAlbumModel());
        when(storesServiceClient.getStoreByStoreId("s1"))
                .thenReturn(entity.getStoreLocationModel());
        when(orderRequestMapper.requestModelToEntity(eq(req), any(), any(), any(), any()))
                .thenReturn(entity);
        when(orderRepository.save(entity))
                .thenReturn(entity);
        var dummyResp = new OrderResponseModel();
        when(orderResponseMapper.entityToResponseModel(entity))
                .thenReturn(dummyResp);

        var resp = orderService.createOrder(req, CUST_ID);
        assertNotNull(resp);
        verify(orderRequestMapper, times(1))
                .requestModelToEntity(eq(req), any(), any(), any(), any());
        verify(orderRepository, times(1)).save(entity);
    }

    @Test
    public void whenUnknownCustomer_thenThrowInvalidOnCreate() {
        var req = OrderRequestModel.builder()
                .artistId("a1").albumId("al1")
                .storeId("s1").orderPrice(50.0)
                .paymentMethod(PaymentMethod.CASH)
                .build();
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(null);
        assertThrows(InvalidInputException.class,
                () -> orderService.createOrder(req, CUST_ID));
    }

    @Test
    public void whenPriceTooLow_thenPatchAndCreate() {
        var entity = buildOrderEntity();
        var req = OrderRequestModel.builder()
                .artistId("a1").albumId("al1")
                .storeId("s1").orderPrice(5.0)
                .paymentMethod(PaymentMethod.CASH)
                .build();
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(entity.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId("a1","al1"))
                .thenReturn(entity.getAlbumModel());
        when(storesServiceClient.getStoreByStoreId("s1"))
                .thenReturn(entity.getStoreLocationModel());
        when(musicCatalogServiceClient.patchAlbumConditionTypeByArtistAndAlbumId("a1","al1", Status.BARGAIN))
                .thenReturn(entity.getAlbumModel());
        when(orderRequestMapper.requestModelToEntity(eq(req), any(), any(), any(), any()))
                .thenReturn(entity);
        when(orderRepository.save(entity))
                .thenReturn(entity);
        when(orderResponseMapper.entityToResponseModel(entity))
                .thenReturn(new OrderResponseModel());

        var resp = orderService.createOrder(req, CUST_ID);
        assertNotNull(resp);
        verify(musicCatalogServiceClient, times(1))
                .patchAlbumConditionTypeByArtistAndAlbumId("a1","al1", Status.BARGAIN);
    }

    //update
    @Test
    public void whenValidUpdate_thenReturnUpdated() {
        var entity = buildOrderEntity();
        var req = OrderRequestModel.builder()
                .artistId("a1").albumId("al1")
                .storeId("s1").orderPrice(123.45)
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .build();
        when(orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(CUST_ID, ORDER_ID))
                .thenReturn(entity);
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(entity.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId("a1","al1"))
                .thenReturn(entity.getAlbumModel());
        when(storesServiceClient.getStoreByStoreId("s1"))
                .thenReturn(entity.getStoreLocationModel());
        when(orderRequestMapper.requestModelToEntity(eq(req), eq(entity.getOrderIdentifier()), any(), any(), any()))
                .thenReturn(entity);
        when(orderRepository.save(entity)).thenReturn(entity);
        when(orderResponseMapper.entityToResponseModel(entity))
                .thenReturn(new OrderResponseModel());

        var resp = orderService.updateOrder(req, CUST_ID, ORDER_ID);
        assertNotNull(resp);
        verify(orderRepository, times(1)).save(entity);
    }

    @Test
    public void whenOrderNotFound_thenThrowInvalidOnUpdate() {
        when(orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(CUST_ID, ORDER_ID))
                .thenReturn(null);
        assertThrows(InvalidInputException.class,
                () -> orderService.updateOrder(
                        OrderRequestModel.builder()
                                .artistId("a1").albumId("al1")
                                .storeId("s1").orderPrice(10.0)
                                .paymentMethod(PaymentMethod.CASH)
                                .build(),
                        CUST_ID, ORDER_ID));
    }

    @Test
    public void whenInvalidPrice_thenThrowInvalidOrderPriceOnUpdate() {
        when(orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(CUST_ID, ORDER_ID))
                .thenReturn(buildOrderEntity());
        assertThrows(InvalidOrderPriceException.class,
                () -> orderService.updateOrder(
                        OrderRequestModel.builder()
                                .artistId("a1").albumId("al1")
                                .storeId("s1").orderPrice(0.0)
                                .paymentMethod(PaymentMethod.CASH)
                                .build(),
                        CUST_ID, ORDER_ID));
    }

    //Delete
    @Test
    public void whenValidDelete_thenDeletes() {
        var entity = buildOrderEntity();
        when(orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(CUST_ID, ORDER_ID))
                .thenReturn(entity);
        assertDoesNotThrow(() -> orderService.deleteOrder(CUST_ID, ORDER_ID));
        verify(orderRepository, times(1)).delete(entity);
    }

    @Test
    public void whenOrderNotFound_thenThrowInvalidOnDelete() {
        when(orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(CUST_ID, ORDER_ID))
                .thenReturn(null);
        assertThrows(InvalidInputException.class,
                () -> orderService.deleteOrder(CUST_ID, ORDER_ID));
    }

    // ==== CREATE ====

    @Test
    void whenValidInput_thenCreateOrder() {
        // arrange
        Order entity = buildOrderEntity();
        var req = OrderRequestModel.builder()
                .artistId(entity.getAlbumModel().getArtistId())
                .albumId(entity.getAlbumModel().getAlbumId())
                .storeId(entity.getStoreLocationModel().getStoreId())
                .orderPrice(42.0)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        // stub out dependencies
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(entity.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId(req.getArtistId(), req.getAlbumId()))
                .thenReturn(entity.getAlbumModel());
        when(storesServiceClient.getStoreByStoreId(req.getStoreId()))
                .thenReturn(entity.getStoreLocationModel());
        when(orderRequestMapper.requestModelToEntity(
                eq(req),
                any(OrderIdentifier.class),
                eq(entity.getAlbumModel()),
                eq(entity.getCustomerModel()),
                eq(entity.getStoreLocationModel())))
                .thenReturn(entity);
        when(orderRepository.save(entity))
                .thenReturn(entity);

        // create a dummy response to return from the spy
        OrderResponseModel dummy = new OrderResponseModel();

        // THIS is the crucial bit: use doReturn() on your spy
        doReturn(dummy)
                .when(orderResponseMapper)
                .entityToResponseModel(entity);

        // act
        OrderResponseModel resp = orderService.createOrder(req, CUST_ID);

        // assert
        assertNotNull(resp);
        assertSame(dummy, resp);

        verify(orderRepository, times(1)).save(entity);
        verify(orderResponseMapper, times(1)).entityToResponseModel(entity);
    }

    @Test
    void whenInvalidCustomerId_thenThrowInvalidInputException() {
        var req = buildOrderEntityRequest();
        when(customersServiceClient.getCustomerByCustomerId("bad-cust"))
                .thenReturn(null);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> orderService.createOrder(req, "bad-cust")
        );
        assertEquals("Unknown customerId provided: bad-cust", ex.getMessage());
    }

    @Test
    void whenInvalidAlbumId_thenThrowInvalidInputException() {
        Order e = buildOrderEntity();
        var req = buildOrderEntityRequest();
        req.setAlbumId("wrong-album");
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(e.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId(CUST_ID, "wrong-album"))
                .thenReturn(null);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> orderService.createOrder(req, CUST_ID)
        );
        assertEquals(
                "No album with id: wrong-album for artistId: " + req.getArtistId(),
                ex.getMessage());
    }

    @Test
    void whenAlbumUnavailable_thenThrowInvalidInputException() {
        Order e = buildOrderEntity();
        var req = buildOrderEntityRequest();
        e.getAlbumModel().setStatus(Status.UNAVAILABLE);
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(e.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId(req.getArtistId(), req.getAlbumId()))
                .thenReturn(e.getAlbumModel());

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> orderService.createOrder(req, CUST_ID)
        );
        assertEquals(
                "Album “" + e.getAlbumModel().getAlbumTitle() + "” is unavailable and cannot be ordered",
                ex.getMessage());
    }

    @Test
    void whenInvalidStoreId_thenThrowInvalidInputException() {
        Order e = buildOrderEntity();
        var req = buildOrderEntityRequest();
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(e.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId(req.getArtistId(), req.getAlbumId()))
                .thenReturn(e.getAlbumModel());
        when(storesServiceClient.getStoreByStoreId("bad-store"))
                .thenReturn(null);
        req.setStoreId("bad-store");

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> orderService.createOrder(req, CUST_ID)
        );
        assertEquals("Unknown storeId provided: bad-store", ex.getMessage());
    }

    @Test
    void whenInvalidOrderPrice_thenThrowInvalidOrderPriceException() {
        Order e = buildOrderEntity();
        var req = buildOrderEntityRequest();
        req.setOrderPrice(0.0);
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(e.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId(req.getArtistId(), req.getAlbumId()))
                .thenReturn(e.getAlbumModel());
        when(storesServiceClient.getStoreByStoreId(req.getStoreId()))
                .thenReturn(e.getStoreLocationModel());

        InvalidOrderPriceException ex = assertThrows(
                InvalidOrderPriceException.class,
                () -> orderService.createOrder(req, CUST_ID)
        );
        assertEquals("Order price must be greater than 0: 0.0", ex.getMessage());
    }

    @Test
    void whenOrderPriceIsNullOrZero_thenThrowInvalidOrderPriceException() {
        // arrange
        var existing = buildOrderEntity();
        var req = OrderRequestModel.builder()
                .artistId("artist-id")
                .albumId("album-id")
                .storeId("store-id")
                .orderPrice(0.0) // Invalid price
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        // stub the lookup so we get past the "unknown order" check
        when(orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(CUST_ID, ORDER_ID))
                .thenReturn(existing);

        // now assert price check happens first
        InvalidOrderPriceException ex = assertThrows(
                InvalidOrderPriceException.class,
                () -> orderService.updateOrder(req, CUST_ID, ORDER_ID)
        );
        assertEquals("Order price must be greater than 0: 0.0", ex.getMessage());
    }

    // 2) Price < 10 should trigger a PATCH and then a re-GET
    @Test
    void whenOrderPriceLessThan10_thenUpdateAlbumConditionAndFetchUpdatedAlbum() {
        // arrange
        var existing = buildOrderEntity();
        var req = OrderRequestModel.builder()
                .artistId("artist-id")
                .albumId("album-id")
                .storeId("store-id")
                .orderPrice(5.0) // < 10
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        // stub repository lookup
        when(orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(CUST_ID, ORDER_ID))
                .thenReturn(existing);

        // stub downstream calls
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(existing.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId("artist-id", "album-id"))
                .thenReturn(existing.getAlbumModel());
        when(storesServiceClient.getStoreByStoreId("store-id"))
                .thenReturn(existing.getStoreLocationModel());

        // patch returns same album
        when(musicCatalogServiceClient
                .patchAlbumConditionTypeByArtistAndAlbumId("artist-id", "album-id", Status.BARGAIN))
                .thenReturn(existing.getAlbumModel());

        // ensure the mapper never returns null
        when(orderRequestMapper.requestModelToEntity(
                eq(req),
                any(OrderIdentifier.class),
                any(AlbumModel.class),
                any(CustomerModel.class),
                any(StoreLocationModel.class)))
                .thenReturn(existing);

        // stub save + response mapper so we don’t blow up at the end
        when(orderRepository.save(existing)).thenReturn(existing);
        doReturn(new OrderResponseModel())
                .when(orderResponseMapper).entityToResponseModel(existing);

        // act
        orderService.updateOrder(req, CUST_ID, ORDER_ID);

        // assert
        verify(musicCatalogServiceClient, times(1))
                .patchAlbumConditionTypeByArtistAndAlbumId("artist-id", "album-id", Status.BARGAIN);
        verify(musicCatalogServiceClient, times(2))
                .getAlbumByAlbumId("artist-id", "album-id");  // once before patch, once after
    }

    // 3) Missing artistName should trigger a second GET of the artist and set it on the entity
    @Test
    void whenArtistNameIsMissing_thenFetchAndSetArtistName() {
        // arrange
        var existing = buildOrderEntity();
        var req = OrderRequestModel.builder()
                .artistId("artist-id")
                .albumId("album-id")
                .storeId("store-id")
                .orderPrice(15.0) // >= 10, so no patch
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        // make the first GET return an album with empty artistName
        var partialAlbum = AlbumModel.builder()
                .artistId("artist-id")
                .albumId("album-id")
                .albumTitle("Album Title")
                .status(Status.NEW)
                .artistName("") // missing
                .build();

        var artistOnly = AlbumModel.builder()
                .artistId("artist-id")
                .artistName("Fetched Artist")
                .build();

        // stub lookups
        when(orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(CUST_ID, ORDER_ID))
                .thenReturn(existing);
        when(customersServiceClient.getCustomerByCustomerId(CUST_ID))
                .thenReturn(existing.getCustomerModel());
        when(musicCatalogServiceClient.getAlbumByAlbumId("artist-id", "album-id"))
                .thenReturn(partialAlbum);
        when(musicCatalogServiceClient.getArtistByArtistId("artist-id"))
                .thenReturn(artistOnly);
        when(storesServiceClient.getStoreByStoreId("store-id"))
                .thenReturn(existing.getStoreLocationModel());
        when(orderRequestMapper.requestModelToEntity(
                eq(req),
                any(OrderIdentifier.class),
                any(AlbumModel.class),
                any(CustomerModel.class),
                any(StoreLocationModel.class)))
                .thenReturn(existing);
        when(orderRepository.save(existing)).thenReturn(existing);
        doReturn(new OrderResponseModel())
                .when(orderResponseMapper).entityToResponseModel(existing);

        // act
        orderService.updateOrder(req, CUST_ID, ORDER_ID);

        // assert
        verify(musicCatalogServiceClient, times(1))
                .getArtistByArtistId("artist-id");
        assertEquals("Fetched Artist", partialAlbum.getArtistName());
    }

    // helper to keep it DRY
    private OrderRequestModel buildOrderEntityRequest() {
        Order e = buildOrderEntity();
        return OrderRequestModel.builder()
                .artistId(e.getAlbumModel().getArtistId())
                .albumId(e.getAlbumModel().getAlbumId())
                .storeId(e.getStoreLocationModel().getStoreId())
                .orderPrice(e.getOrderPrice())
                .paymentMethod(e.getPaymentMethod())
                .build();
    }

}

