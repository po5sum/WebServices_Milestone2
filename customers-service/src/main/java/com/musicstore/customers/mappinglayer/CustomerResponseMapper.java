package com.musicstore.customers.mappinglayer;


import com.musicstore.customers.dataaccesslayer.Customer;
import com.musicstore.customers.presentationlayer.CustomerController;
import com.musicstore.customers.presentationlayer.CustomerResponseModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface CustomerResponseMapper {

    @Mapping(expression = "java(customer.getCustomerIdentifier().getCustomerId())", target = "customerId")
    @Mapping(expression = "java(customer.getCustomerAddress().getStreetAddress())", target = "streetAddress")
    @Mapping(expression = "java(customer.getCustomerAddress().getCity())", target = "city")
    @Mapping(expression = "java(customer.getCustomerAddress().getProvince())", target = "province")
    @Mapping(expression = "java(customer.getCustomerAddress().getCountry())", target = "country")
    @Mapping(expression = "java(customer.getCustomerAddress().getPostalCode())", target = "postalCode")
    CustomerResponseModel entityToResponseModel(Customer customer);

    List<CustomerResponseModel> entityListToResponseModelList(List<Customer> customers);

    @AfterMapping
    default void addLinks(@MappingTarget CustomerResponseModel customerResponseModel) {
        Link selfLink = linkTo(methodOn(CustomerController.class)
                .getCustomerByCustomerId(customerResponseModel.getCustomerId()))

                .withSelfRel();
        customerResponseModel.add(selfLink);

        Link AllCustomersLink = linkTo(methodOn(CustomerController.class)
                .getCustomers())
                .withRel("customers");
        customerResponseModel.add(AllCustomersLink);
    }
}
