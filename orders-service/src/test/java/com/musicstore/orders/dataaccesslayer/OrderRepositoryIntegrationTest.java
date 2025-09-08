package com.musicstore.orders.dataaccesslayer;

import com.musicstore.orders.domainclientlayer.customer.CustomerModel;
import com.musicstore.orders.domainclientlayer.musiccatalog.AlbumModel;
import com.musicstore.orders.domainclientlayer.storelocation.StoreLocationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
public class OrderRepositoryIntegrationTest {
    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;

    @BeforeEach
    public void setupDB() {
        orderRepository.deleteAll();

        AlbumModel album1 = AlbumModel.builder()
                .artistId("e5913a79-9b1e-4516-9ffd-06578e7af261")
                .albumId("84c5f33e-8e5d-4eb5-b35d-79272355fa72")
                .artistName("The Beatles")
                .albumTitle("Abbey Road")
                .build();
        CustomerModel customer1 = CustomerModel.builder()
                .customerId("c3540a89-cb47-4c96-888e-ff96708db4d8")
                .firstName("Alick")
                .lastName("Ucceli")
                .build();
        StoreLocationModel store1 = StoreLocationModel.builder()
                .storeId("b2d3a4e7-f29b-4f5e-bf1c-8a77a7319a1e")
                .ownerName("John Doe")
                .managerName("Jane Smith")
                .build();

        order1 = Order.builder()
                .orderIdentifier(new OrderIdentifier())
                .albumModel(album1)
                .customerModel(customer1)
                .storeLocationModel(store1)
                .orderDate(LocalDate.of(2025, 4, 10))
                .orderStatus(OrderStatus.PENDING)
                .orderPrice(19.99)
                .paymentMethod(PaymentMethod.PAYPAL)
                .build();


        AlbumModel album2 = AlbumModel.builder()
                .artistId("ed5536b9-3bca-4eef-b7c0-13d800babde4")
                .albumId("36966db2-a18c-42b6-8d03-dad7fc7e3ea3")
                .artistName("Kanye West")
                .albumTitle("The College Dropout")
                .build();
        CustomerModel customer2 = CustomerModel.builder()
                .customerId("dd1ab8b0-ab17-4e03-b70a-84caa3871606")
                .firstName("Ricky")
                .lastName("Presslie")
                .build();
        StoreLocationModel store2 = StoreLocationModel.builder()
                .storeId("d8e9f1a2-b34c-4d5e-9f6a-8b7c6a2310f5")
                .ownerName("Michael Scott")
                .managerName("Pam Beesly")
                .build();

        order2 = Order.builder()
                .orderIdentifier(new OrderIdentifier())
                .albumModel(album2)
                .customerModel(customer2)
                .storeLocationModel(store2)
                .orderDate(LocalDate.of(2025, 4, 11))
                .orderStatus(OrderStatus.SHIPPED)
                .orderPrice(29.99)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        orderRepository.save(order1);
        orderRepository.save(order2);
    }

    @Test
    public void whenCustomerIdFound_thenReturnOrdersList() {
        List<Order> found = orderRepository
                .findAllByCustomerModel_CustomerId(order1.getCustomerModel().getCustomerId());

        assertNotNull(found);
        assertEquals(1, found.size());
        assertEquals(
                order1.getOrderIdentifier().getOrderId(),
                found.get(0).getOrderIdentifier().getOrderId()
        );
    }

    @Test
    public void whenFindAllByCustomerIdNotFound_thenReturnEmptyList() {
        List<Order> empty = orderRepository
                .findAllByCustomerModel_CustomerId("no‐such‐customer");
        assertNotNull(empty);
        assertTrue(empty.isEmpty());
    }


    @Test
    public void whenOrderIdFound_thenReturnOrder() {
        Order found = orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(
                        order2.getCustomerModel().getCustomerId(),
                        order2.getOrderIdentifier().getOrderId()
                );

        assertNotNull(found);
        assertEquals(order2.getOrderIdentifier().getOrderId(),
                found.getOrderIdentifier().getOrderId());
        assertEquals(order2.getCustomerModel().getCustomerId(),
                found.getCustomerModel().getCustomerId());
    }

    @Test
    public void whenOrderNotFound_thenReturnNull() {
        Order missing = orderRepository
                .findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(
                        "no-such-customer",
                        "no-such-order"
                );
        assertNull(missing);
    }

    @Test
    public void whenSaveNewOrder_thenAssignIds() {
        AlbumModel album = AlbumModel.builder()
                .artistId("9a8f2c41-5c6b-4a0f-8b78-7f5a3e2c9f77")
                .albumId("d3a4f8c5-6f22-4b2b-9e29-7d1b8df2c672")
                .artistName("David Bowie")
                .albumTitle("Ziggy Stardust")
                .build();
        CustomerModel customer = CustomerModel.builder()
                .customerId("ba6c3e76-366e-44bb-8279-b41dc32dc456")
                .firstName("Allx")
                .lastName("Cholmondeley")
                .build();
        StoreLocationModel store = StoreLocationModel.builder()
                .storeId("k7l8m9n0-c12d-0e3f-g45h-6i7j8k9l0m1n")
                .ownerName("Rick Sanchez")
                .managerName("Morty Smith")
                .build();

        Order newOrder = Order.builder()
                .orderIdentifier(new OrderIdentifier())
                .albumModel(album)
                .customerModel(customer)
                .storeLocationModel(store)
                .orderDate(LocalDate.of(2025, 4, 12))
                .orderStatus(OrderStatus.DELIVERED)
                .orderPrice(39.99)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        Order saved = orderRepository.save(newOrder);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertNotNull(saved.getOrderIdentifier().getOrderId());
        assertEquals(3, orderRepository.count());
    }

    @Test
    public void whenDeleteOrder_thenDecreaseCount() {
        long before = orderRepository.count();
        orderRepository.delete(order1);
        assertEquals(before - 1, orderRepository.count());
    }

    @Test
    public void whenDeleteNonExistentOrder_thenNoExceptionThrown() {
        Order ghost = Order.builder()
                .orderIdentifier(new OrderIdentifier())
                .albumModel(order1.getAlbumModel())
                .customerModel(order1.getCustomerModel())
                .storeLocationModel(order1.getStoreLocationModel())
                .orderDate(LocalDate.now())
                .orderStatus(OrderStatus.CANCELLED)
                .orderPrice(0.0)
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .build();


        ghost.setId("ghost-id");

        assertDoesNotThrow(() -> orderRepository.delete(ghost));
    }

    @Test
    public void whenFindAll_thenReturnAllOrders() {
        List<Order> all = orderRepository.findAll();
        assertNotNull(all);
        assertEquals(2, all.size());
        List<String> ids = all.stream()
                .map(o -> o.getOrderIdentifier().getOrderId())
                .collect(Collectors.toList());
        assertTrue(ids.contains(order1.getOrderIdentifier().getOrderId()));
        assertTrue(ids.contains(order2.getOrderIdentifier().getOrderId()));
    }

    @Test
    public void whenFindAllEmpty_thenReturnEmptyList() {
        orderRepository.deleteAll();
        List<Order> all = orderRepository.findAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    public void whenUpdateExistingOrder_thenPersistChanges() {
        // change status & price
        order1.setOrderStatus(OrderStatus.DELIVERED);
        order1.setOrderPrice(99.99);
        orderRepository.save(order1);

        Order found = orderRepository.findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(
                order1.getCustomerModel().getCustomerId(),
                order1.getOrderIdentifier().getOrderId()
        );
        assertNotNull(found);
        assertEquals(OrderStatus.DELIVERED, found.getOrderStatus());
        assertEquals(99.99, found.getOrderPrice());
    }

    @Test
    public void whenSaveNonExistentOrder_thenCreateNewRecord() {
        long before = orderRepository.count();
        Order ghost = Order.builder()
                .orderIdentifier(new OrderIdentifier())
                .albumModel(order1.getAlbumModel())
                .customerModel(order1.getCustomerModel())
                .storeLocationModel(order1.getStoreLocationModel())
                .orderDate(LocalDate.now())
                .orderStatus(OrderStatus.CANCELLED)
                .orderPrice(0.0)
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .build();

        Order saved = orderRepository.save(ghost);

        assertNotNull(saved.getId());
        assertEquals(before + 1, orderRepository.count());
    }

    @Test
    public void whenExistsById_thenTrue() {
        assertTrue(orderRepository.existsById(order1.getId()));
    }

    @Test
    public void whenExistsById_thenFalse() {
        assertFalse(orderRepository.existsById("no-such-id"));
    }
}

