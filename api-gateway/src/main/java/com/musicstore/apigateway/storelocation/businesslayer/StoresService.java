package com.musicstore.apigateway.storelocation.businesslayer;


import com.musicstore.apigateway.storelocation.presentationlayer.StoreRequestModel;
import com.musicstore.apigateway.storelocation.presentationlayer.StoreResponseModel;

import java.util.List;

public interface StoresService {
    List<StoreResponseModel> getAllStores();
    StoreResponseModel getStoreByStoreId(String storeId);
    StoreResponseModel addStore(StoreRequestModel storeRequestModel);
    StoreResponseModel updateStore(StoreRequestModel storeRequestModel, String storeId);
    void deleteStore(String storeId);
}
