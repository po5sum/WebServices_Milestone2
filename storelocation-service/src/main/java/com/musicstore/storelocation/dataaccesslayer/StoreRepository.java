package com.musicstore.storelocation.dataaccesslayer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Integer> {
    Store findStoreByStoreIdentifier_StoreId(String storeId);
    boolean existsByStoreAddress_StreetAddress(String streetAddress);
}
