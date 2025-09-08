package com.musicstore.apigateway.storelocation.businesslayer;

import com.musicstore.apigateway.storelocation.domainclientlayer.StoresServiceClient;
import com.musicstore.apigateway.storelocation.presentationlayer.StoreRequestModel;
import com.musicstore.apigateway.storelocation.presentationlayer.StoreResponseModel;
import com.musicstore.apigateway.storelocation.presentationlayer.StoresController;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class StoresServiceImpl implements StoresService {

    private final StoresServiceClient storesServiceClient;

    public StoresServiceImpl(StoresServiceClient storesServiceClient) {
        this.storesServiceClient = storesServiceClient;
    }

    @Override
    public List<StoreResponseModel> getAllStores() {
        List<StoreResponseModel> stores = storesServiceClient.getAllStores();
        if (stores != null) {
            for (StoreResponseModel store : stores) {
                addLinks(store);
            }
        }
        return stores;
    }

    @Override
    public StoreResponseModel getStoreByStoreId(String storeId) {
        StoreResponseModel store = storesServiceClient.getStoreByStoreId(storeId);
        if (store != null) {
            addLinks(store);
        }
        return store;
    }

    @Override
    public StoreResponseModel addStore(StoreRequestModel storeRequestModel) {
        StoreResponseModel store = storesServiceClient.addStore(storeRequestModel);
        if (store != null) {
            addLinks(store);
        }
        return store;
    }

    @Override
    public StoreResponseModel updateStore(StoreRequestModel storeRequestModel, String storeId) {
        StoreResponseModel store = storesServiceClient.updateStore(storeRequestModel, storeId);
        if (store != null) {
            addLinks(store);
        }
        return store;
    }

    @Override
    public void deleteStore(String storeId) {
        storesServiceClient.deleteStore(storeId);
    }

    private StoreResponseModel addLinks(StoreResponseModel store) {
        Link selfLink = linkTo(methodOn(StoresController.class).getStoreByStoreId(store.getStoreId())).withSelfRel();
        Link allStoresLink = linkTo(methodOn(StoresController.class).getAllStores()).withRel("stores");

        store.add(selfLink);
        store.add(allStoresLink);

        return store;
    }
}
