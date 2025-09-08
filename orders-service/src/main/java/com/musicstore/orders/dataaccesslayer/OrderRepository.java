package com.musicstore.orders.dataaccesslayer;


import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    //Order findByOrderIdentifier_OrderId(String orderId);
    Order findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(String customerId, String orderId);
    List<Order> findAllByCustomerModel_CustomerId(String customerId);
    void deleteByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(String customerId, String orderId);
}


