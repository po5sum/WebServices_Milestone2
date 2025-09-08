package com.musicstore.orders.utils;

import com.musicstore.orders.dataaccesslayer.*;
import com.musicstore.orders.domainclientlayer.customer.CustomerModel;
import com.musicstore.orders.domainclientlayer.musiccatalog.AlbumModel;
import com.musicstore.orders.domainclientlayer.storelocation.StoreLocationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DatabaseLoaderService implements CommandLineRunner {

    @Autowired
    OrderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {
        var orderIdentifier = new OrderIdentifier();
        var album = AlbumModel.builder()
                .artistId("e5913a79-9b1e-4516-9ffd-06578e7af261")       // The Beatles
                .albumId("84c5f33e-8e5d-4eb5-b35d-79272355fa72")        // Abbey Road
                .artistName("The Beatles")
                .albumTitle("Abbey Road")
                .build();

        var customer = CustomerModel.builder()
                .customerId("c3540a89-cb47-4c96-888e-ff96708db4d8")      // Alick Ucceli
                .firstName("Alick")
                .lastName("Ucceli")
                .build();

        var store = StoreLocationModel.builder()
                .storeId("b2d3a4e7-f29b-4f5e-bf1c-8a77a7319a1e")        // Montreal store
                .ownerName("John Doe")
                .managerName("Jane Smith")
                .build();

        var sampleOrder = Order.builder()
                .orderIdentifier(orderIdentifier)                 // auto-generates UUID
                .albumModel(album)
                .customerModel(customer)
                .storeLocationModel(store)
                .orderDate(LocalDate.of(2025, 4, 10))
                .orderStatus(OrderStatus.SHIPPED)
                .orderPrice(29.99)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        orderRepository.save(sampleOrder);

    }
}
