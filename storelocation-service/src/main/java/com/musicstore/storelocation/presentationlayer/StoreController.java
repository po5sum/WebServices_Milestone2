package com.musicstore.storelocation.presentationlayer;

import com.musicstore.storelocation.businesslayer.StoreService;
import com.musicstore.storelocation.utils.exceptions.InvalidInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/stores")
public class StoreController {

    private final StoreService storeService;
    private static final int UUID_LENGTH = 36;

    @Autowired
    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public ResponseEntity<List<StoreResponseModel>> getAllStores() {
        List<StoreResponseModel> stores = storeService.getAllStores();
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponseModel> getStoreByStoreId(@PathVariable String storeId) {
        if (storeId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid storeId provided: " + storeId);
        }
        StoreResponseModel store = storeService.getStoreByStoreId(storeId);
        return ResponseEntity.ok(store);
    }

    @PostMapping
    public ResponseEntity<StoreResponseModel> addStore(@RequestBody StoreRequestModel storeRequestModel) {
        StoreResponseModel createdStore = storeService.addStore(storeRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<StoreResponseModel> updateStore(@RequestBody StoreRequestModel storeRequestModel, @PathVariable String storeId) {
        if (storeId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid storeId provided: " + storeId);
        }
        StoreResponseModel updatedStore = storeService.updateStore(storeRequestModel, storeId);
        return ResponseEntity.ok(updatedStore);
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable String storeId) {
        if (storeId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid storeId provided: " + storeId);
        }
        storeService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }
}