package com.musicstore.orders.presentationlayer;


import com.musicstore.orders.dataaccesslayer.OrderStatus;
import com.musicstore.orders.dataaccesslayer.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseModel extends RepresentationModel<OrderResponseModel> {
    String orderId;
    String artistId;
    String artistName; //get from Artist Service
    String albumId;
    String albumTitle; //get from Album Service
    String customerId;
    String customerFirstName; //get from Customer service
    String customerLastName; //get from Customer service
    String storeId;
    String ownerName; //get from Store Service
    String managerName; //get from Store Service
    LocalDate orderDate;
    OrderStatus orderStatus;
    Double orderPrice;
    PaymentMethod paymentMethod;

}
