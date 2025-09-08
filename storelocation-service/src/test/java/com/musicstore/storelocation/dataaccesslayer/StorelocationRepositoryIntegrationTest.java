package com.musicstore.storelocation.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class StorelocationRepositoryIntegrationTest {
    @Autowired
    private StoreRepository storeRepository;

    @BeforeEach
    public void setUp() {
        storeRepository.deleteAll();
    }
    @Test
    public void whenStoreExists_thenReturnAllStores() {
        Store store1 = new Store(
                new StoreInformation("Owner1", "Manager1", 4.5, "123-456-7890", "store1@example.com", "9-5"),
                new StoreAddress("123 Street", "CityA", "ProvinceA", "A1A1A1")
        );

        Store store2 = new Store(
                new StoreInformation("Owner2", "Manager2", 4.0, "987-654-3210", "store2@example.com", "10-6"),
                new StoreAddress("456 Avenue", "CityB", "ProvinceB", "B2B2B2")
        );

        storeRepository.save(store1);
        storeRepository.save(store2);

        List<Store> stores = storeRepository.findAll();
        assertEquals(2, stores.size());
    }

    @Test
    public void whenFindByStoreId_thenReturnStore() {
        Store store = new Store(
                new StoreInformation("Lookup Owner", "Lookup Manager", 5.0, "111-222-3333", "lookup@store.com", "10-8"),
                new StoreAddress("789 Lookup", "LookCity", "LookProv", "L3L3L3")
        );

        Store savedStore = storeRepository.save(store);
        String storeId = savedStore.getStoreIdentifier().getStoreId();

        Store foundStore = storeRepository.findStoreByStoreIdentifier_StoreId(storeId);
        assertNotNull(foundStore);
        assertEquals("Lookup Owner", foundStore.getStoreInformation().getOwnerName());
    }

    @Test
    public void whenStoreNotFound_thenReturnNull() {
        Store store = storeRepository.findStoreByStoreIdentifier_StoreId("nonexistent-id");
        assertNull(store);
    }

    @Test
    public void whenValidStore_thenPersistAndRetrieve() {
        Store store = new Store(
                new StoreInformation("Test Owner", "Test Manager", 3.8, "321-321-4321", "test@store.com", "8-4"),
                new StoreAddress("101 Test Rd", "Testville", "Testprov", "T1T1T1")
        );

        Store savedStore = storeRepository.save(store);
        assertNotNull(savedStore.getId());
        assertNotNull(savedStore.getStoreIdentifier());
        assertEquals("Test Owner", savedStore.getStoreInformation().getOwnerName());
    }

    @Test
    public void whenUpdateStore_thenReturnUpdatedStore() {
        // arrange: save original store
        StoreInformation originalInfo = new StoreInformation("Owner", "Manager", 3.5, "123-456-7890", "email@store.com", "8-5");
        StoreAddress address = new StoreAddress("456 Update", "Update City", "Province", "P0S7UP");

        Store store = new Store(originalInfo, address);
        Store savedStore = storeRepository.save(store);

// update: create new StoreInformation instance
        StoreInformation updatedInfo = new StoreInformation("New Owner", "New Manager", 4.9, "999-999-9999", "newemail@store.com", "9-6");

// act: assign new embedded object
        savedStore.setStoreInformation(updatedInfo);
        Store updatedStore = storeRepository.save(savedStore);

// assert
        assertEquals("New Owner", updatedStore.getStoreInformation().getOwnerName());
    }

    @Test
    public void whenUpdateNonExistentStore_thenCreateNewRecord() {
        Store ghost = new Store(
                new StoreInformation("Ghost", "Phantom", 1.0, "000-000-0000", "ghost@store.com", "Closed"),
                new StoreAddress("999 Nowhere", "GhostCity", "PhantomProv", "00000")
        );

        long countBefore = storeRepository.count();
        Store saved = storeRepository.save(ghost);

        assertNotNull(saved.getId());
        assertEquals(countBefore + 1, storeRepository.count());
    }


    @Test
    public void whenDeleteStore_thenNotFoundAfter() {
        Store store = new Store(
                new StoreInformation("To Delete", "Manager", 1.0, "000-000-0000", "delete@store.com", "Closed"),
                new StoreAddress("000 Void", "Ghosttown", "None", "00000")
        );

        Store savedStore = storeRepository.save(store);
        String storeId = savedStore.getStoreIdentifier().getStoreId();

        storeRepository.delete(savedStore);
        Store deleted = storeRepository.findStoreByStoreIdentifier_StoreId(storeId);
        assertNull(deleted);
    }
    @Test
    public void whenDeleteNonExistentStore_thenNoExceptionThrown() {
        Store ghost = new Store(
                new StoreInformation("Ghost", "None", 1.0, "000-000-0000", "ghost@store.com", "Closed"),
                new StoreAddress("Unknown", "Nowhere", "None", "00000")
        );

        assertDoesNotThrow(() -> storeRepository.delete(ghost));
    }


    @Test
    public void whenStoreExistsByStreetAddress_thenReturnTrue() {
        Store store = new Store(
                new StoreInformation("Jane Doe", "Manager", 4.0, "123-123-1234", "jane@store.com", "8-5"),
                new StoreAddress("999 Unique St", "Town", "Prov", "99999")
        );

        storeRepository.save(store);
        assertTrue(storeRepository.existsByStoreAddress_StreetAddress("999 Unique St"));
    }

    @Test
    public void whenStoreDoesNotExistByStreetAddress_thenReturnFalse() {
        assertFalse(storeRepository.existsByStoreAddress_StreetAddress("Nonexistent Street"));
    }
}
