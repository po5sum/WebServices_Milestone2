package com.musicstore.apigateway.presentationlayer.storelocation;

import com.musicstore.apigateway.storelocation.presentationlayer.StoreRequestModel;
import com.musicstore.apigateway.storelocation.presentationlayer.StoreResponseModel;
import com.musicstore.apigateway.storelocation.presentationlayer.StoresController;
import com.musicstore.apigateway.storelocation.businesslayer.StoresService;
import com.musicstore.apigateway.utils.exceptions.InvalidInputException;
import com.musicstore.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class StoresControllerUnitTest {
    @Autowired
    private StoresController storesController;

    @MockitoBean
    private StoresService storesService;

    private final String VALID_STORE_ID     = "b2d3a4e7-f29b-4f5e-bf1c-8a77a7319a1e";
    private final String INVALID_STORE_ID   = "not-a-uuid";
    private final String NOT_FOUND_STORE_ID = "d9f3a4e7-f29b-4f5e-bf1c-8a77a7319a1f";

    private StoreRequestModel sampleRequest() {
        return StoreRequestModel.builder()
                .ownerName("Alice")
                .managerName("Bob")
                .storeRating(4.0)
                .phoneNumber("555-0000")
                .email("alice@example.com")
                .openHours("9-5")
                .streetAddress("123 Main St")
                .city("Townsville")
                .province("Prov")
                .postalCode("12345")
                .build();
    }

    private StoreResponseModel sampleResponse(String id) {
        return StoreResponseModel.builder()
                .storeId(id)
                .ownerName("Alice")
                .managerName("Bob")
                .storeRating(4.0)
                .phoneNumber("555-0000")
                .email("alice@example.com")
                .openHours("9-5")
                .streetAddress("123 Main St")
                .city("Townsville")
                .province("Prov")
                .postalCode("12345")
                .build();
    }

    @Test
    void whenGetAllStores_thenReturnList() {
        var respList = List.of(
                sampleResponse("id1"),
                sampleResponse("id2")
        );
        when(storesService.getAllStores()).thenReturn(respList);

        ResponseEntity<List<StoreResponseModel>> resp = storesController.getAllStores();

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(2, resp.getBody().size());
        verify(storesService, times(1)).getAllStores();
    }

    @Test
    void whenGetStoreByValidId_thenReturnStore() {
        var expected = sampleResponse(VALID_STORE_ID);
        when(storesService.getStoreByStoreId(VALID_STORE_ID)).thenReturn(expected);

        ResponseEntity<StoreResponseModel> resp =
                storesController.getStoreByStoreId(VALID_STORE_ID);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(expected, resp.getBody());
        verify(storesService).getStoreByStoreId(VALID_STORE_ID);
    }

    @Test
    void whenGetStoreInvalidId_thenThrowInvalidInput() {
        when(storesService.getStoreByStoreId(INVALID_STORE_ID))
                .thenThrow(new InvalidInputException("Invalid id"));

        assertThrows(
                InvalidInputException.class,
                () -> storesController.getStoreByStoreId(INVALID_STORE_ID)
        );
        verify(storesService).getStoreByStoreId(INVALID_STORE_ID);
    }

    @Test
    void whenGetStoreNotFound_thenThrowNotFound() {
        when(storesService.getStoreByStoreId(NOT_FOUND_STORE_ID))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(
                NotFoundException.class,
                () -> storesController.getStoreByStoreId(NOT_FOUND_STORE_ID)
        );
        verify(storesService).getStoreByStoreId(NOT_FOUND_STORE_ID);
    }

    @Test
    void whenCreateStoreValid_thenReturnCreated() {
        var req = sampleRequest();
        var created = sampleResponse(UUID.randomUUID().toString());
        when(storesService.addStore(req)).thenReturn(created);

        ResponseEntity<StoreResponseModel> resp = storesController.addStore(req);

        assertEquals(201, resp.getStatusCodeValue());
        assertSame(created, resp.getBody());
        verify(storesService).addStore(req);
    }

    @Test
    void whenCreateStoreDuplicate_thenThrowInvalidInput() {
        var req = sampleRequest();
        when(storesService.addStore(req))
                .thenThrow(new InvalidInputException("duplicate"));

        assertThrows(
                InvalidInputException.class,
                () -> storesController.addStore(req)
        );
        verify(storesService).addStore(req);
    }

    @Test
    void whenUpdateStoreValid_thenReturnOk() {
        var req = sampleRequest();
        var updated = sampleResponse(VALID_STORE_ID);
        when(storesService.updateStore(req, VALID_STORE_ID)).thenReturn(updated);

        ResponseEntity<StoreResponseModel> resp =
                storesController.updateStore(req, VALID_STORE_ID);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(updated, resp.getBody());
        verify(storesService).updateStore(req, VALID_STORE_ID);
    }

    @Test
    void whenUpdateStoreInvalidId_thenThrowInvalidInput() {
        var req = sampleRequest();
        when(storesService.updateStore(req, INVALID_STORE_ID))
                .thenThrow(new InvalidInputException("Invalid id"));

        assertThrows(
                InvalidInputException.class,
                () -> storesController.updateStore(req, INVALID_STORE_ID)
        );
        verify(storesService).updateStore(req, INVALID_STORE_ID);
    }

    @Test
    void whenUpdateStoreNotFound_thenThrowNotFound() {
        var req = sampleRequest();
        when(storesService.updateStore(req, NOT_FOUND_STORE_ID))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(
                NotFoundException.class,
                () -> storesController.updateStore(req, NOT_FOUND_STORE_ID)
        );
        verify(storesService).updateStore(req, NOT_FOUND_STORE_ID);
    }

    @Test
    void whenDeleteStoreValid_thenReturnNoContent() {
        // service.deleteStore() does nothing
        doNothing().when(storesService).deleteStore(VALID_STORE_ID);

        ResponseEntity<Void> resp =
                storesController.deleteStore(VALID_STORE_ID);

        assertEquals(204, resp.getStatusCodeValue());
        verify(storesService).deleteStore(VALID_STORE_ID);
    }

    @Test
    void whenDeleteStoreInvalidId_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid id"))
                .when(storesService).deleteStore(INVALID_STORE_ID);

        assertThrows(
                InvalidInputException.class,
                () -> storesController.deleteStore(INVALID_STORE_ID)
        );
        verify(storesService).deleteStore(INVALID_STORE_ID);
    }

    @Test
    void whenDeleteStoreNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException("Not found"))
                .when(storesService).deleteStore(NOT_FOUND_STORE_ID);

        assertThrows(
                NotFoundException.class,
                () -> storesController.deleteStore(NOT_FOUND_STORE_ID)
        );
        verify(storesService).deleteStore(NOT_FOUND_STORE_ID);
    }
}
