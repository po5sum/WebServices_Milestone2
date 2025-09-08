package com.musicstore.apigateway.orders.businesslayer;

import com.musicstore.apigateway.orders.presentationlayer.OrdersRequestModel;
import com.musicstore.apigateway.orders.presentationlayer.OrdersResponseModel;

import java.util.List;

public interface OrdersService {
    List<OrdersResponseModel> getAllOrdersByCustomerId(String customerId);
    OrdersResponseModel findOrderBydOrderId(String customerId, String orderId);
    OrdersResponseModel createOrder(OrdersRequestModel orderRequestModel, String customerId);
    OrdersResponseModel updateOrder(OrdersRequestModel orderRequestModel, String customerId,String orderId);
    void deleteOrder(String customerId, String orderId);
}


