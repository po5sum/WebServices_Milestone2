package com.musicstore.apigateway.orders.businesslayer;

import com.musicstore.apigateway.orders.domainclientlayer.OrdersServiceClient;
import com.musicstore.apigateway.orders.presentationlayer.OrdersController;
import com.musicstore.apigateway.orders.presentationlayer.OrdersRequestModel;
import com.musicstore.apigateway.orders.presentationlayer.OrdersResponseModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class OrdersServiceImpl implements OrdersService{
    private final OrdersServiceClient ordersServiceClient;

    public OrdersServiceImpl(OrdersServiceClient ordersServiceClient) {
        this.ordersServiceClient = ordersServiceClient;
    }

    @Override
    public List<OrdersResponseModel> getAllOrdersByCustomerId(String customerId) {
        List<OrdersResponseModel> orders = ordersServiceClient.getOrdersByCustomerId(customerId);
        if (orders != null) {
            orders.forEach(o -> addLinks(customerId, o));
        }
        return orders;
    }

    @Override
    public OrdersResponseModel findOrderBydOrderId(String customerId, String orderId) {
        OrdersResponseModel order = ordersServiceClient.getOrderByOrderId(customerId, orderId);
        if (order != null) {
            addLinks(customerId, order);
        }
        return order;
    }

    @Override
    public OrdersResponseModel createOrder(OrdersRequestModel orderRequestModel, String customerId) {
        OrdersResponseModel order = ordersServiceClient.addOrder(orderRequestModel, customerId);
        if (order != null) {
            addLinks(customerId, order);
        }
        return order;
    }

    @Override
    public OrdersResponseModel updateOrder(OrdersRequestModel orderRequestModel, String customerId, String orderId) {
        OrdersResponseModel order = ordersServiceClient.updateOrder(orderRequestModel, customerId, orderId);
        if (order != null) {
            addLinks(customerId, order);
        }
        return order;
    }

    @Override
    public void deleteOrder(String customerId, String orderId) {
        ordersServiceClient.removeOrder(customerId, orderId);
    }
    private void addLinks(String customerId, OrdersResponseModel order) {

        Link self = linkTo(methodOn(OrdersController.class)
                .findOrderBydOrderId(customerId, order.getOrderId()))
                .withSelfRel();

        Link customerOrders = linkTo(methodOn(OrdersController.class)
                .getAllOrdersByCustomerId(customerId))
                .withRel("customerOrders");

        order.add(self);
        order.add(customerOrders);
    }
}
