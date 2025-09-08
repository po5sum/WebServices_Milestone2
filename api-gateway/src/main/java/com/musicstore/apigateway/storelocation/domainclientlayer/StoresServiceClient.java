package com.musicstore.apigateway.storelocation.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.apigateway.storelocation.presentationlayer.StoreRequestModel;
import com.musicstore.apigateway.storelocation.presentationlayer.StoreResponseModel;
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
public class StoresServiceClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String STORES_SERVICE_BASE_URL;

    public StoresServiceClient(RestTemplate restTemplate,
                               ObjectMapper mapper,
                               @Value("${app.storelocation-service.host}") String storesServiceHost,
                               @Value("${app.storelocation-service.port}") String storesServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        STORES_SERVICE_BASE_URL = "http://" + storesServiceHost + ":" + storesServicePort + "/api/v1/stores";
    }

    public List<StoreResponseModel> getAllStores() {
        try {
            String url = STORES_SERVICE_BASE_URL;
            log.debug("Stores-Service GET all stores URL: " + url);
            ResponseEntity<List<StoreResponseModel>> response =
                    restTemplate.exchange(url, HttpMethod.GET, null,
                            new ParameterizedTypeReference<List<StoreResponseModel>>() {});
            List<StoreResponseModel> storeResponseModels = response.getBody();
            return storeResponseModels;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public StoreResponseModel getStoreByStoreId(String storeId) {
        try {
            String url = STORES_SERVICE_BASE_URL + "/" + storeId;
            log.debug("Stores-Service GET by storeId URL: " + url);
            StoreResponseModel storeResponseModel = restTemplate.getForObject(url, StoreResponseModel.class);
            return storeResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public StoreResponseModel addStore(StoreRequestModel storeRequestModel) {
        try {
            String url = STORES_SERVICE_BASE_URL;
            log.debug("Stores-Service POST URL: " + url);
            StoreResponseModel storeResponseModel =
                    restTemplate.postForObject(url, storeRequestModel, StoreResponseModel.class);
            return storeResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public StoreResponseModel updateStore(StoreRequestModel storeRequestModel, String storeId) {
        try {
            String url = STORES_SERVICE_BASE_URL + "/" + storeId;
            log.debug("Stores-Service PUT URL: " + url);
            HttpEntity<StoreRequestModel> requestEntity = new HttpEntity<>(storeRequestModel);
            ResponseEntity<StoreResponseModel> response =
                    restTemplate.exchange(url, HttpMethod.PUT, requestEntity, StoreResponseModel.class);
            StoreResponseModel storeResponseModel = response.getBody();
            return storeResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteStore(String storeId) {
        try {
            String url = STORES_SERVICE_BASE_URL + "/" + storeId;
            log.debug("Stores-Service DELETE URL: " + url);
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
