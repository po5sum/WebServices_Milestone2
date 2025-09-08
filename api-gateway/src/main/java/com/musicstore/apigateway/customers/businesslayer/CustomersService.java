package com.musicstore.apigateway.customers.businesslayer;

import com.musicstore.apigateway.customers.presentationlayer.CustomerRequestModel;
import com.musicstore.apigateway.customers.presentationlayer.CustomerResponseModel;

import java.util.List;

public interface CustomersService {
    List<CustomerResponseModel> getAllCustomers();
    CustomerResponseModel getCustomerByCustomerId(String customerId);
    CustomerResponseModel addCustomer(CustomerRequestModel customerRequestModel);
    CustomerResponseModel updateCustomer(CustomerRequestModel updatedCustomer, String customerId);
    void removeCustomer(String customerId);
}
