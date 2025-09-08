package com.musicstore.storelocation.businesslayer;

import com.musicstore.storelocation.presentationlayer.StoreRequestModel;
import com.musicstore.storelocation.presentationlayer.StoreResponseModel;

import java.util.List;

public interface StoreService {
    List<StoreResponseModel> getAllStores();
    StoreResponseModel getStoreByStoreId(String storeId);
    StoreResponseModel addStore(StoreRequestModel storeRequestModel);
    StoreResponseModel updateStore(StoreRequestModel storeRequestModel, String storeId);
    void deleteStore(String storeId);
}
