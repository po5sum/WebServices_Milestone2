package com.musicstore.apigateway.orders.presentationlayer;


import com.musicstore.apigateway.orders.businesslayer.OrdersService;
import com.musicstore.apigateway.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/customers/{customerId}/orders")
public class OrdersController {
    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<OrdersResponseModel>> getAllOrdersByCustomerId(
            @PathVariable String customerId) {

        log.debug("Request received in OrdersController: getAllOrdersByCustomerId");
        List<OrdersResponseModel> orders = ordersService.getAllOrdersByCustomerId(customerId);
        return ResponseEntity.ok().body(orders);
    }

    @GetMapping(value = "/{orderId}", produces = "application/json")
    public ResponseEntity<OrdersResponseModel> findOrderBydOrderId(@PathVariable String customerId,
                                                                   @PathVariable String orderId) {
        log.debug("Request received in OrdersController: findOrderBydOrderId");
        OrdersResponseModel order = ordersService.findOrderBydOrderId(customerId, orderId);
        return ResponseEntity.ok().body(order);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<OrdersResponseModel> createOrder(@RequestBody OrdersRequestModel orderRequestModel,
                                                           @PathVariable String customerId) {
        log.debug("Request received in OrdersController: createOrder");
        OrdersResponseModel order =
                ordersService.createOrder(orderRequestModel, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PutMapping(value = "/{orderId}",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<OrdersResponseModel> updateOrder(@RequestBody OrdersRequestModel orderRequestModel,
                                                           @PathVariable String customerId,
                                                           @PathVariable String orderId) {
        log.debug("Request received in OrdersController: updateOrder");
        OrdersResponseModel order =
                ordersService.updateOrder(orderRequestModel, customerId, orderId);
        return ResponseEntity.ok().body(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String customerId, @PathVariable String orderId) {
        log.debug("Request received in OrdersController: deleteOrder");
        ordersService.deleteOrder(customerId, orderId);
        return ResponseEntity.noContent().build();
    }
}