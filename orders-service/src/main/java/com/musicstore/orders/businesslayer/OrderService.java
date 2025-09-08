package com.musicstore.orders.businesslayer;



import com.musicstore.orders.presentationlayer.OrderRequestModel;
import com.musicstore.orders.presentationlayer.OrderResponseModel;

import java.util.List;

public interface OrderService {
    List<OrderResponseModel> getAllOrdersByCustomerId(String customerId);
    OrderResponseModel findOrderBydOrderId(String customerId, String orderId);
    OrderResponseModel createOrder(OrderRequestModel orderRequestModel, String customerId);
    OrderResponseModel updateOrder(OrderRequestModel orderRequestModel, String customerId,String orderId);
    void deleteOrder(String customerId, String orderId);
}


