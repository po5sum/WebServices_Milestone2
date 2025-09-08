package com.musicstore.orders.dataaccesslayer;


import com.musicstore.orders.domainclientlayer.customer.CustomerModel;
import com.musicstore.orders.domainclientlayer.musiccatalog.AlbumModel;
import com.musicstore.orders.domainclientlayer.storelocation.StoreLocationModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;


@Document(collection = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private String id;

    private OrderIdentifier orderIdentifier;
    private AlbumModel albumModel;
    private CustomerModel customerModel;
    private StoreLocationModel storeLocationModel;
    private LocalDate orderDate;
    private OrderStatus orderStatus;
    private Double orderPrice;
    private PaymentMethod paymentMethod;
}

