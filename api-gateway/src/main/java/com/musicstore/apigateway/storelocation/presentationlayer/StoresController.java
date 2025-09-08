package com.musicstore.apigateway.storelocation.presentationlayer;

import com.musicstore.apigateway.storelocation.businesslayer.StoresService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/stores")
public class StoresController {
    private final StoresService storesService;

    public StoresController(StoresService storesService) {
        this.storesService = storesService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<StoreResponseModel>> getAllStores() {
        log.debug("Request received in StoresController: getAllStores");
        List<StoreResponseModel> stores = storesService.getAllStores();
        return ResponseEntity.ok().body(stores);
    }

    @GetMapping(value = "/{storeId}", produces = "application/json")
    public ResponseEntity<StoreResponseModel> getStoreByStoreId(
            @PathVariable("storeId") String storeId) {
        log.debug("Request received in StoresController: getStoreByStoreId");
        StoreResponseModel store = storesService.getStoreByStoreId(storeId);
        return ResponseEntity.ok().body(store);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<StoreResponseModel> addStore(
            @RequestBody StoreRequestModel storeRequestModel) {
        log.debug("Request received in StoresController: addStore");
        StoreResponseModel store = storesService.addStore(storeRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(store);
    }

    @PutMapping(value = "/{storeId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<StoreResponseModel> updateStore(
            @RequestBody StoreRequestModel storeRequestModel,
            @PathVariable("storeId") String storeId) {
        log.debug("Request received in StoresController: updateStore");
        StoreResponseModel store = storesService.updateStore(storeRequestModel, storeId);
        return ResponseEntity.ok().body(store);
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable("storeId") String storeId) {
        log.debug("Request received in StoresController: deleteStore");
        storesService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }
}
