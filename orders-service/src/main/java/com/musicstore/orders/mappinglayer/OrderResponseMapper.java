package com.musicstore.orders.mappinglayer;

import com.musicstore.orders.dataaccesslayer.Order;
import com.musicstore.orders.presentationlayer.OrderController;
import com.musicstore.orders.presentationlayer.OrderResponseModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface OrderResponseMapper {
    @Mapping(expression = "java(order.getOrderIdentifier().getOrderId())", target = "orderId")
    @Mapping(expression = "java(order.getAlbumModel().getArtistId())", target = "artistId")
    @Mapping(expression = "java(order.getAlbumModel().getArtistName())", target = "artistName")
    @Mapping(expression = "java(order.getAlbumModel().getAlbumId())", target = "albumId")
    @Mapping(expression = "java(order.getAlbumModel().getAlbumTitle())", target = "albumTitle")
    @Mapping(expression = "java(order.getCustomerModel().getCustomerId())", target = "customerId")
    @Mapping(expression = "java(order.getCustomerModel().getFirstName())", target = "customerFirstName")
    @Mapping(expression = "java(order.getCustomerModel().getLastName())", target = "customerLastName")
    @Mapping(expression = "java(order.getStoreLocationModel().getStoreId())", target = "storeId")
    @Mapping(expression = "java(order.getStoreLocationModel().getOwnerName())", target = "ownerName")
    @Mapping(expression = "java(order.getStoreLocationModel().getManagerName())", target = "managerName")
    @Mapping(expression = "java(order.getOrderDate())", target = "orderDate")
    @Mapping(expression = "java(order.getOrderStatus())", target = "orderStatus")
    @Mapping(expression = "java(order.getOrderPrice())", target = "orderPrice")
    @Mapping(expression = "java(order.getPaymentMethod())", target = "paymentMethod")
    OrderResponseModel entityToResponseModel(Order order);

    List<OrderResponseModel> entityListToResponseModelList(List<Order> orders);

    @AfterMapping
    default void addOrderLinks(@MappingTarget OrderResponseModel response) {
        // Self link: GET /api/v1/customers/{customerId}/orders/{orderId}
        Link selfLink = linkTo(methodOn(OrderController.class)
                .findOrderBydOrderId(response.getCustomerId(), response.getOrderId()))
                .withSelfRel();
        response.add(selfLink);

        // All orders for this customer: GET /api/v1/customers/{customerId}/orders
        Link allOrdersLink = linkTo(methodOn(OrderController.class)
                .getAllOrdersByCustomerId(response.getCustomerId()))
                .withRel("allOrdersInCustomer");
        response.add(allOrdersLink);

        // Link to the customer: GET /api/v1/customers/{customerId}
        Link customerLink = Link.of("http://localhost:8081/api/v1/customers/" + response.getCustomerId())
                .withRel("customer");
        response.add(customerLink);

        // Link to all customers: GET /api/v1/customers
        Link allCustomersLink = Link.of("http://localhost:8081/api/v1/customers")
                .withRel("allCustomers");
        response.add(allCustomersLink);
    }
}