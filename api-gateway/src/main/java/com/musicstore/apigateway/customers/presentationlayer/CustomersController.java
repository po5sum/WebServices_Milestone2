package com.musicstore.apigateway.customers.presentationlayer;

import com.musicstore.apigateway.customers.businesslayer.CustomersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/customers")
public class CustomersController {
    private final CustomersService customersService;

    public CustomersController(CustomersService customersService) {
        this.customersService = customersService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<CustomerResponseModel>> getAllCustomers() {
        log.debug("Request received in CustomersController: getCustomers");
        List<CustomerResponseModel> customers = customersService.getAllCustomers();
        return ResponseEntity.ok().body(customers);
    }

    @GetMapping(value = "/{customerId}", produces = "application/json")
    public ResponseEntity<CustomerResponseModel> getCustomerByCustomerId(
            @PathVariable("customerId") String customerId) {
        log.debug("Request received in CustomersController: getCustomerByCustomerId");
        CustomerResponseModel customer = customersService.getCustomerByCustomerId(customerId);
        return ResponseEntity.ok().body(customer);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<CustomerResponseModel> addCustomer(
            @RequestBody CustomerRequestModel customerRequestModel) {
        log.debug("Request received in CustomersController: addCustomer");
        CustomerResponseModel customer = customersService.addCustomer(customerRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PutMapping(value = "/{customerId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CustomerResponseModel> updateCustomer(
            @RequestBody CustomerRequestModel customerRequestModel,
            @PathVariable("customerId") String customerId) {
        log.debug("Request received in CustomersController: updateCustomer");
        CustomerResponseModel customer = customersService.updateCustomer(customerRequestModel, customerId);
        return ResponseEntity.ok().body(customer);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> removeCustomer(@PathVariable("customerId") String customerId) {
        log.debug("Request received in CustomersController: removeCustomer");
        customersService.removeCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
}
