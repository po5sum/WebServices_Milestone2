package com.musicstore.customers.businesslayer;



import com.musicstore.customers.dataaccesslayer.Customer;
import com.musicstore.customers.dataaccesslayer.CustomerAddress;
import com.musicstore.customers.dataaccesslayer.CustomerIdentifier;
import com.musicstore.customers.dataaccesslayer.CustomerRepository;
import com.musicstore.customers.mappinglayer.CustomerRequestMapper;
import com.musicstore.customers.mappinglayer.CustomerResponseMapper;
import com.musicstore.customers.presentationlayer.CustomerRequestModel;
import com.musicstore.customers.presentationlayer.CustomerResponseModel;
import com.musicstore.customers.utils.exceptions.DuplicateEmailException;
import com.musicstore.customers.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerResponseMapper customerResponseMapper;
    private final CustomerRequestMapper customerRequestMapper;


    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerResponseMapper customerResponseMapper, CustomerRequestMapper customerRequestMapper) {
        this.customerRepository = customerRepository;
        this.customerResponseMapper = customerResponseMapper;
        this.customerRequestMapper = customerRequestMapper;
    }

    @Override
    public List<CustomerResponseModel> getCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customerResponseMapper.entityListToResponseModelList(customers);
    }

    @Override
    public CustomerResponseModel getCustomerByCustomerId(String customerId) {
        Customer customer = customerRepository.findByCustomerIdentifier_CustomerId(customerId);

        if (customer == null) {
            throw new NotFoundException("Provided customerId not found: " + customerId);
        }
        return customerResponseMapper.entityToResponseModel(customer);
    }

    @Override
    public CustomerResponseModel addCustomer(CustomerRequestModel customerRequestModel) {
        if (customerRepository.existsByEmailAddress(customerRequestModel.getEmailAddress())) {
            throw new DuplicateEmailException("Customer with the same email address already exists.");
        }

        CustomerAddress address = new CustomerAddress(customerRequestModel.getStreetAddress(), customerRequestModel.getCity(),
            customerRequestModel.getProvince(), customerRequestModel.getCountry(), customerRequestModel.getPostalCode());

        Customer customer = customerRequestMapper.requestModelToEntity(customerRequestModel, new CustomerIdentifier(), address);

        customer.setCustomerAddress(address);
        return customerResponseMapper.entityToResponseModel(customerRepository.save(customer));
    }

    @Override
    public CustomerResponseModel updateCustomer(CustomerRequestModel customerRequestModel, String customerId) {

        Customer existingCustomer = customerRepository.findByCustomerIdentifier_CustomerId(customerId);

        //check if a customer with the provided UUID exists in the system. If it doesn't, return null
        //later, when we implement exception handling, we'll return an exception
        if (existingCustomer == null) {
            throw new NotFoundException("Provided customerId not found: " + customerId);
        }
        if (!existingCustomer.getEmailAddress().equals(customerRequestModel.getEmailAddress()) &&
                customerRepository.existsByEmailAddress(customerRequestModel.getEmailAddress())) {
            throw new DuplicateEmailException("Customer with the same email address already exists.");
        }
        CustomerAddress address = new CustomerAddress(customerRequestModel.getStreetAddress(), customerRequestModel.getCity(),
            customerRequestModel.getProvince(), customerRequestModel.getCountry(), customerRequestModel.getPostalCode());
        Customer updatedCustomer = customerRequestMapper.requestModelToEntity(customerRequestModel,
            existingCustomer.getCustomerIdentifier(), address);
        updatedCustomer.setId(existingCustomer.getId());

        Customer response = customerRepository.save(updatedCustomer);
        return customerResponseMapper.entityToResponseModel(response);
    }

    @Override
    public void removeCustomer(String customerId) {
        Customer existingCustomer = customerRepository.findByCustomerIdentifier_CustomerId(customerId);

        if (existingCustomer == null) {
            throw new NotFoundException("Provided customerId not found: " + customerId);
        }

        customerRepository.delete(existingCustomer);
    }
}
