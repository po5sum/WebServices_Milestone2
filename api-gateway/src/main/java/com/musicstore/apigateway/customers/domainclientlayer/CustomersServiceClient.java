package com.musicstore.apigateway.customers.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.apigateway.customers.presentationlayer.CustomerRequestModel;
import com.musicstore.apigateway.customers.presentationlayer.CustomerResponseModel;
import com.musicstore.apigateway.utils.HttpErrorInfo;
import com.musicstore.apigateway.utils.exceptions.InvalidInputException;
import com.musicstore.apigateway.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Component
public class CustomersServiceClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String CUSTOMERS_SERVICE_BASE_URL;

    public CustomersServiceClient(RestTemplate restTemplate,
                                  ObjectMapper mapper,
                                  @Value("${app.customers-service.host}") String customersServiceHost,
                                  @Value("${app.customers-service.port}") String customersServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        CUSTOMERS_SERVICE_BASE_URL = "http://" + customersServiceHost + ":" + customersServicePort + "/api/v1/customers";
    }

    public List<CustomerResponseModel> getAllCustomers() {
        try {
            String url = CUSTOMERS_SERVICE_BASE_URL;
            log.debug("Customers-Service GET all customers URL: " + url);
            ResponseEntity<List<CustomerResponseModel>> response =
                    restTemplate.exchange(url, HttpMethod.GET, null,
                            new ParameterizedTypeReference<List<CustomerResponseModel>>() {});
            List<CustomerResponseModel> customerResponseModels = response.getBody();
            return customerResponseModels;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CustomerResponseModel getCustomerByCustomerId(String customerId) {
        try {
            String url = CUSTOMERS_SERVICE_BASE_URL + "/" + customerId;
            log.debug("Customers-Service GET by customerId URL: " + url);
            CustomerResponseModel customerResponseModel = restTemplate.getForObject(url, CustomerResponseModel.class);
            return customerResponseModel;
        }
        catch(HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CustomerResponseModel addCustomer(CustomerRequestModel customerRequestModel) {
        try {
            String url = CUSTOMERS_SERVICE_BASE_URL;
            log.debug("Customers-Service POST URL: " + url);
            CustomerResponseModel customerResponseModel =
                    restTemplate.postForObject(url, customerRequestModel, CustomerResponseModel.class);
            return customerResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CustomerResponseModel updateCustomer(CustomerRequestModel customerRequestModel, String customerId) {
        try {
            String url = CUSTOMERS_SERVICE_BASE_URL + "/" + customerId;
            log.debug("Customers-Service PUT URL: " + url);
            HttpEntity<CustomerRequestModel> requestEntity = new HttpEntity<>(customerRequestModel);
            ResponseEntity<CustomerResponseModel> response =
                    restTemplate.exchange(url, HttpMethod.PUT, requestEntity, CustomerResponseModel.class);
            CustomerResponseModel customerResponseModel = response.getBody();
            return customerResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void removeCustomer(String customerId) {
        try {
            String url = CUSTOMERS_SERVICE_BASE_URL + "/" + customerId;
            log.debug("Customers-Service DELETE URL: " + url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }
        catch (IOException ioex) {
            return ioex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(getErrorMessage(ex));
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(getErrorMessage(ex));
        }
        log.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
        log.warn("Error body: {}", ex.getResponseBodyAsString());
        return ex;
    }
}