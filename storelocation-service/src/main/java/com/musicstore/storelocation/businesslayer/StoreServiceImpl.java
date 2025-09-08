package com.musicstore.storelocation.businesslayer;

import com.musicstore.storelocation.dataaccesslayer.*;
import com.musicstore.storelocation.mappinglayer.StoreRequestMapper;
import com.musicstore.storelocation.mappinglayer.StoreResponseMapper;
import com.musicstore.storelocation.presentationlayer.StoreRequestModel;
import com.musicstore.storelocation.presentationlayer.StoreResponseModel;
import com.musicstore.storelocation.utils.exceptions.DuplicateAddressException;
import com.musicstore.storelocation.utils.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final StoreRequestMapper storeRequestMapper;
    private final StoreResponseMapper storeResponseMapper;

    public StoreServiceImpl(StoreRepository storeRepository, StoreRequestMapper storeRequestMapper, StoreResponseMapper storeResponseMapper) {
        this.storeRepository = storeRepository;
        this.storeRequestMapper = storeRequestMapper;
        this.storeResponseMapper = storeResponseMapper;
    }

    @Override
    public List<StoreResponseModel> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return storeResponseMapper.entityListToResponseModelList(stores);
    }

    @Override
    public StoreResponseModel getStoreByStoreId(String storeId) {
        Store store = storeRepository.findStoreByStoreIdentifier_StoreId(storeId);

        if(store == null) {
            throw new NotFoundException("Store not found" + storeId);
        }
        return storeResponseMapper.entityToResponseModel(store);
    }

    @Override
    public StoreResponseModel addStore(StoreRequestModel storeRequestModel) {
        StoreInformation storeInformation = new StoreInformation(storeRequestModel.getOwnerName(), storeRequestModel.getManagerName(),
                storeRequestModel.getStoreRating(), storeRequestModel.getPhoneNumber(), storeRequestModel.getEmail(), storeRequestModel.getOpenHours());

        StoreAddress storeAddress = new StoreAddress(storeRequestModel.getStreetAddress(), storeRequestModel.getCity(),
                storeRequestModel.getProvince(), storeRequestModel.getPostalCode());

        if (storeRepository.existsByStoreAddress_StreetAddress(storeRequestModel.getStreetAddress())) {
            throw new DuplicateAddressException("Store with the same street address already exists.");
        }

        Store store = storeRequestMapper.requestModelToEntity(storeRequestModel, new StoreIdentifier(), storeInformation, storeAddress);
        store.setStoreInformation(storeInformation);
        store.setStoreAddress(storeAddress);

        return storeResponseMapper.entityToResponseModel(storeRepository.save(store));
    }

    @Override
    public StoreResponseModel updateStore(StoreRequestModel storeRequestModel, String storeId) {
        Store existingStore = storeRepository.findStoreByStoreIdentifier_StoreId(storeId);

        if(existingStore == null) {
            throw new NotFoundException("Store not found" + storeId);
        }

        if (!existingStore.getStoreAddress().getStreetAddress().equals(storeRequestModel.getStreetAddress()) &&
                storeRepository.existsByStoreAddress_StreetAddress(storeRequestModel.getStreetAddress())) {
            throw new DuplicateAddressException("Store with the same street address already exists.");
        }

        StoreInformation storeInformation = new StoreInformation(storeRequestModel.getOwnerName(), storeRequestModel.getManagerName(),
                storeRequestModel.getStoreRating(), storeRequestModel.getPhoneNumber(), storeRequestModel.getEmail(), storeRequestModel.getOpenHours());

        StoreAddress storeAddress = new StoreAddress(storeRequestModel.getStreetAddress(), storeRequestModel.getCity(),
                storeRequestModel.getProvince(), storeRequestModel.getPostalCode());

        Store updatedStore = storeRequestMapper.requestModelToEntity(storeRequestModel, existingStore.getStoreIdentifier(),
                storeInformation, storeAddress);

        updatedStore.setId(existingStore.getId());
        Store response = storeRepository.save(updatedStore);
        return storeResponseMapper.entityToResponseModel(response);
    }

    @Override
    public void deleteStore(String storeId) {
        Store existingStore = storeRepository.findStoreByStoreIdentifier_StoreId(storeId);
        if(existingStore == null) {
            throw new NotFoundException("Store not found" + storeId);
        }
        storeRepository.delete(existingStore);
    }
}
