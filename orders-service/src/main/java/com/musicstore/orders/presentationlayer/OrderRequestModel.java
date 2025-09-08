package com.musicstore.orders.presentationlayer;

import com.musicstore.orders.dataaccesslayer.OrderStatus;
import com.musicstore.orders.dataaccesslayer.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestModel {
    String artistId;
    String albumId;
    String storeId;
    LocalDate orderDate;
    OrderStatus orderStatus;
    Double orderPrice;
    PaymentMethod paymentMethod;
}