package com.musicstore.orders.presentationlayer;

import com.musicstore.orders.businesslayer.OrderService;
import com.musicstore.orders.utils.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/customers/{customerId}/orders")
public class OrderController {
    private final OrderService orderService;
    private static final int UUID_LENGTH = 36;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseModel>> getAllOrdersByCustomerId(@PathVariable String customerId) {
        if (customerId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid customerId provided: " + customerId);
        }
        return ResponseEntity.ok(orderService.getAllOrdersByCustomerId(customerId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseModel> findOrderBydOrderId(@PathVariable String customerId,@PathVariable String orderId) {
        if (customerId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid customerId provided: " + customerId);
        }
        if (orderId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid orderId provided: " + orderId);
        }
        return ResponseEntity.ok(orderService.findOrderBydOrderId(customerId, orderId));
    }

    @PostMapping
    public ResponseEntity<OrderResponseModel> createOrder(@RequestBody OrderRequestModel orderRequestModel, @PathVariable String customerId) {
        if (customerId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid customerId provided: " + customerId);
        }
        OrderResponseModel created = orderService.createOrder(orderRequestModel, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponseModel> updateOrder(@PathVariable String customerId, @PathVariable String orderId,
                                                          @RequestBody OrderRequestModel orderRequestModel) {
        if (customerId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid customerId provided: " + customerId);
        }
        if (orderId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid orderId provided: " + orderId);
        }
        return ResponseEntity.ok(orderService.updateOrder(orderRequestModel, customerId, orderId));
    }
    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable String customerId, @PathVariable String orderId) {
        if (customerId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid customerId provided: " + customerId);
        }
        if (orderId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid orderId provided: " + orderId);
        }
        orderService.deleteOrder(customerId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body("Order deleted successfully");
    }
}