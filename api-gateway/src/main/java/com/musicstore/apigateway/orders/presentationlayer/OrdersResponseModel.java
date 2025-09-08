package com.musicstore.apigateway.orders.presentationlayer;

import com.musicstore.apigateway.orders.domainclientlayer.OrderStatus;
import com.musicstore.apigateway.orders.domainclientlayer.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrdersResponseModel extends RepresentationModel<OrdersResponseModel> {
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
    String orderDate;
    OrderStatus orderStatus;
    Double orderPrice;
    PaymentMethod paymentMethod;

}
