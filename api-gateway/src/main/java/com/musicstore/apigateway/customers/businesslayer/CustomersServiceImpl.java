package com.musicstore.apigateway.customers.businesslayer;

import com.musicstore.apigateway.customers.domainclientlayer.CustomersServiceClient;
import com.musicstore.apigateway.customers.presentationlayer.CustomerRequestModel;
import com.musicstore.apigateway.customers.presentationlayer.CustomerResponseModel;
import com.musicstore.apigateway.customers.presentationlayer.CustomersController;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class CustomersServiceImpl implements CustomersService {

    private final CustomersServiceClient customersServiceClient;

    public CustomersServiceImpl(CustomersServiceClient customersServiceClient) {
        this.customersServiceClient = customersServiceClient;
    }

    @Override
    public List<CustomerResponseModel> getAllCustomers() {
        List<CustomerResponseModel> customers = customersServiceClient.getAllCustomers();
        if (customers != null) {
            for (CustomerResponseModel customer : customers) {
                addLinks(customer);
            }
        }
        return customers;
    }

    @Override
    public CustomerResponseModel getCustomerByCustomerId(String customerId) {
        CustomerResponseModel customer = customersServiceClient.getCustomerByCustomerId(customerId);
        if (customer != null) {
            addLinks(customer);
        }
        return customer;
    }

    @Override
    public CustomerResponseModel addCustomer(CustomerRequestModel customerRequestModel) {
        CustomerResponseModel customer = customersServiceClient.addCustomer(customerRequestModel);
        if (customer != null) {
            addLinks(customer);
        }
        return customer;
    }

    @Override
    public CustomerResponseModel updateCustomer(CustomerRequestModel updatedCustomer, String customerId) {
        CustomerResponseModel customer = customersServiceClient.updateCustomer(updatedCustomer, customerId);
        if (customer != null) {
            addLinks(customer);
        }
        return customer;
    }

    @Override
    public void removeCustomer(String customerId) {
        customersServiceClient.removeCustomer(customerId);
    }

    private CustomerResponseModel addLinks(CustomerResponseModel customer) {
        Link selfLink = linkTo(methodOn(CustomersController.class)
                .getCustomerByCustomerId(customer.getCustomerId())).withSelfRel();
        Link allCustomersLink = linkTo(methodOn(CustomersController.class)
                .getAllCustomers()).withRel("customers");

        customer.add(selfLink);
        customer.add(allCustomersLink);

        return customer;
    }
}
