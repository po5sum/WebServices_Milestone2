package com.musicstore.apigateway.orders.presentationlayer;

import com.musicstore.apigateway.orders.domainclientlayer.OrderStatus;
import com.musicstore.apigateway.orders.domainclientlayer.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrdersRequestModel {
    String artistId;
    String albumId;
    String storeId;
    String orderDate;
    OrderStatus orderStatus;
    Double orderPrice;
    PaymentMethod paymentMethod;
}