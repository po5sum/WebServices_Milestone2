package com.musicstore.orders.dataaccesslayer;


import lombok.Getter;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.UUID;

@Getter
public class OrderIdentifier {

    @Indexed(unique = true)
    private String orderId;

    public OrderIdentifier() {
        this.orderId = UUID.randomUUID().toString();
    }
}