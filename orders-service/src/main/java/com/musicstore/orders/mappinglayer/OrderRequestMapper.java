package com.musicstore.orders.mappinglayer;


import com.musicstore.orders.dataaccesslayer.Order;
import com.musicstore.orders.dataaccesslayer.OrderIdentifier;
import com.musicstore.orders.domainclientlayer.customer.CustomerModel;
import com.musicstore.orders.domainclientlayer.musiccatalog.AlbumModel;
import com.musicstore.orders.domainclientlayer.storelocation.StoreLocationModel;
import com.musicstore.orders.presentationlayer.OrderRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface OrderRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(expression = "java(orderIdentifier)", target = "orderIdentifier")
    @Mapping(expression = "java(orderRequestModel.getOrderDate())", target = "orderDate")
    @Mapping(expression = "java(orderRequestModel.getOrderStatus())", target = "orderStatus")
    @Mapping(expression = "java(orderRequestModel.getOrderPrice())", target = "orderPrice")
    @Mapping(expression = "java(orderRequestModel.getPaymentMethod())", target = "paymentMethod")
    Order requestModelToEntity(OrderRequestModel orderRequestModel,
                               OrderIdentifier orderIdentifier,
                               AlbumModel albumModel,
                               CustomerModel customerModel,
                               StoreLocationModel storeLocationModel);
}