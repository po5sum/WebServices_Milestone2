package com.musicstore.apigateway.orders.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.apigateway.orders.presentationlayer.OrdersRequestModel;
import com.musicstore.apigateway.orders.presentationlayer.OrdersResponseModel;
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
public class OrdersServiceClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String ORDERS_SERVICE_BASE_URL;

    public OrdersServiceClient(RestTemplate restTemplate, ObjectMapper mapper,
                               @Value("${app.orders-service.host}") String ordersServiceHost,
                               @Value("${app.orders-service.port}") String ordersServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        ORDERS_SERVICE_BASE_URL = "http://" + ordersServiceHost + ":" + ordersServicePort + "/api/v1/customers";
    }

    // ===== ORDERS METHODS =====
    //get all orders
    public List<OrdersResponseModel> getOrdersByCustomerId(String customerId) {
        String url = ORDERS_SERVICE_BASE_URL + "/" + customerId + "/orders";
        log.debug("Orders-Service GET list URL: {}", url);
        try {
            ResponseEntity<List<OrdersResponseModel>> response =
                    restTemplate.exchange(url, HttpMethod.GET, null,
                            new ParameterizedTypeReference<List<OrdersResponseModel>>() {});
            List<OrdersResponseModel> ordersResponseModels = response.getBody();
            return ordersResponseModels;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    //get order by id
    public OrdersResponseModel getOrderByOrderId(String customerId, String orderId) {
        try {
            String url = ORDERS_SERVICE_BASE_URL + "/" + customerId + "/orders/" + orderId;
            log.debug("Orders-Service GET by ID URL: {}", url);
            OrdersResponseModel ordersResponseModel =
                    restTemplate.getForObject(url, OrdersResponseModel.class);
            return ordersResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    //creat new order
    public OrdersResponseModel addOrder(OrdersRequestModel orderRequestModel, String customerId) {
        try {
            String url = ORDERS_SERVICE_BASE_URL + "/" + customerId + "/orders";
            log.debug("Orders-Service POST URL: {}", url);

            OrdersResponseModel ordersResponseModel =
                    restTemplate.postForObject(url, orderRequestModel, OrdersResponseModel.class);
            return ordersResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    //update order
    public OrdersResponseModel updateOrder(OrdersRequestModel orderRequestModel, String customerId,String orderId) {
        try {
            String url = ORDERS_SERVICE_BASE_URL + "/" + customerId + "/orders/" + orderId;
            log.debug("Orders-Service PUT URL: {}", url);
            HttpEntity<OrdersRequestModel> entity = new HttpEntity<>(orderRequestModel);
            ResponseEntity<OrdersResponseModel> response =
                    restTemplate.exchange(url, HttpMethod.PUT, entity, OrdersResponseModel.class);
            OrdersResponseModel ordersResponseModel = response.getBody();
            return ordersResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    //delete order
    public void removeOrder(String customerId, String orderId) {
        try {
            String url = ORDERS_SERVICE_BASE_URL + "/" + customerId + "/orders/" + orderId;
            log.debug("Orders-Service DELETE URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }



    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
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
        log.warn("Unexpected HTTP error: {}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ex;
    }
}
