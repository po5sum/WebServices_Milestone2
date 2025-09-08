package com.musicstore.orders.domainclientlayer.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class CustomerModel {

    String customerId;
    String firstName;
    String lastName;
}
