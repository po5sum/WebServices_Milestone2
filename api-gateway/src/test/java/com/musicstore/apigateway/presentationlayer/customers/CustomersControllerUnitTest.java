package com.musicstore.apigateway.presentationlayer.customers;

import com.musicstore.apigateway.customers.businesslayer.CustomersService;
import com.musicstore.apigateway.customers.presentationlayer.CustomerRequestModel;
import com.musicstore.apigateway.customers.presentationlayer.CustomerResponseModel;
import com.musicstore.apigateway.customers.domainclientlayer.ContactMethodPreference;
import com.musicstore.apigateway.customers.domainclientlayer.PhoneNumber;
import com.musicstore.apigateway.customers.domainclientlayer.PhoneType;
import com.musicstore.apigateway.customers.presentationlayer.CustomersController;
import com.musicstore.apigateway.utils.exceptions.InvalidInputException;
import com.musicstore.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class CustomersControllerUnitTest {
    @Autowired
    private CustomersController customersController;

    @MockitoBean
    private CustomersService customersService;

    private static final String VALID_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private static final String NOT_FOUND_ID = "c3540a89-cb47-4c96-888e-ff96708db4d7";
    private static final String INVALID_ID = "bad-uuid";

    @Test
    void whenGetAllCustomers_thenReturnList() {
        CustomerResponseModel c1 = CustomerResponseModel.builder()
                .customerId("id1").firstName("John").lastName("Doe").build();
        CustomerResponseModel c2 = CustomerResponseModel.builder()
                .customerId("id2").firstName("Jane").lastName("Roe").build();
        when(customersService.getAllCustomers()).thenReturn(List.of(c1, c2));

        ResponseEntity<List<CustomerResponseModel>> resp = customersController.getAllCustomers();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        verify(customersService, times(1)).getAllCustomers();
    }

    @Test
    void whenGetCustomerByValidId_thenReturnCustomer() {
        CustomerResponseModel cust = CustomerResponseModel.builder()
                .customerId(VALID_ID).firstName("Alice").lastName("Smith").build();
        when(customersService.getCustomerByCustomerId(VALID_ID)).thenReturn(cust);

        ResponseEntity<CustomerResponseModel> resp = customersController.getCustomerByCustomerId(VALID_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(cust, resp.getBody());
        verify(customersService).getCustomerByCustomerId(VALID_ID);
    }

    @Test
    void whenGetCustomerInvalidId_thenThrowInvalidInput() {
        // Arrange
        when(customersService.getCustomerByCustomerId(INVALID_ID))
                .thenThrow(new InvalidInputException("Invalid customerId provided: " + INVALID_ID));

        // Act & Assert
        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> customersController.getCustomerByCustomerId(INVALID_ID)
        );
        assertEquals("Invalid customerId provided: " + INVALID_ID, ex.getMessage());
        verify(customersService, times(1)).getCustomerByCustomerId(INVALID_ID);
    }

    @Test
    void whenGetCustomerNotFound_thenThrowNotFound() {
        when(customersService.getCustomerByCustomerId(NOT_FOUND_ID))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class,
                () -> customersController.getCustomerByCustomerId(NOT_FOUND_ID));
        verify(customersService).getCustomerByCustomerId(NOT_FOUND_ID);
    }

    @Test
    void whenAddCustomerValid_thenReturnCreated() {
        CustomerRequestModel req = CustomerRequestModel.builder()
                .firstName("Bob").lastName("Builder").emailAddress("bob@build.com")
                .contactMethodPreference(ContactMethodPreference.EMAIL)
                .build();
        CustomerResponseModel created = CustomerResponseModel.builder()
                .customerId("newId").firstName("Bob").lastName("Builder").build();
        when(customersService.addCustomer(req)).thenReturn(created);

        ResponseEntity<CustomerResponseModel> resp = customersController.addCustomer(req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertSame(created, resp.getBody());
        verify(customersService).addCustomer(req);
    }

    @Test
    void whenAddCustomerInvalid_thenThrowInvalidInput() {
        CustomerRequestModel req = CustomerRequestModel.builder().build();
        when(customersService.addCustomer(req))
                .thenThrow(new InvalidInputException("Bad email"));

        assertThrows(InvalidInputException.class,
                () -> customersController.addCustomer(req));
        verify(customersService).addCustomer(req);
    }

    @Test
    void whenUpdateCustomerValid_thenReturnOk() {
        CustomerRequestModel req = CustomerRequestModel.builder()
                .firstName("Upd").lastName("Name").build();
        CustomerResponseModel updated = CustomerResponseModel.builder()
                .customerId(VALID_ID).firstName("Upd").lastName("Name").build();
        when(customersService.updateCustomer(req, VALID_ID)).thenReturn(updated);

        ResponseEntity<CustomerResponseModel> resp = customersController.updateCustomer(req, VALID_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(updated, resp.getBody());
        verify(customersService).updateCustomer(req, VALID_ID);
    }

    @Test
    void whenUpdateCustomerInvalidId_thenThrowInvalidInput() {
        CustomerRequestModel req = CustomerRequestModel.builder().build();
        when(customersService.updateCustomer(req, INVALID_ID))
                .thenThrow(new InvalidInputException("Invalid customerId provided: " + INVALID_ID));

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> customersController.updateCustomer(req, INVALID_ID)
        );
        assertEquals("Invalid customerId provided: " + INVALID_ID, ex.getMessage());
        verify(customersService, times(1)).updateCustomer(req, INVALID_ID);
    }

    @Test
    void whenUpdateCustomerNotFound_thenThrowNotFound() {
        CustomerRequestModel req = CustomerRequestModel.builder().build();
        when(customersService.updateCustomer(req, NOT_FOUND_ID))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class,
                () -> customersController.updateCustomer(req, NOT_FOUND_ID));
        verify(customersService).updateCustomer(req, NOT_FOUND_ID);
    }

    @Test
    void whenRemoveCustomerValid_thenReturnNoContent() {
        doNothing().when(customersService).removeCustomer(VALID_ID);

        ResponseEntity<Void> resp = customersController.removeCustomer(VALID_ID);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(customersService).removeCustomer(VALID_ID);
    }

    @Test
    void whenRemoveCustomerInvalidId_thenThrowInvalidInput() {
        // Arrange
        doThrow(new InvalidInputException("Invalid customerId provided: " + INVALID_ID))
                .when(customersService).removeCustomer(INVALID_ID);

        // Act & Assert
        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> customersController.removeCustomer(INVALID_ID)
        );
        assertEquals("Invalid customerId provided: " + INVALID_ID, ex.getMessage());

        verify(customersService, times(1)).removeCustomer(INVALID_ID);
    }


    @Test
    void whenRemoveCustomerNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException("Not found"))
                .when(customersService).removeCustomer(NOT_FOUND_ID);

        assertThrows(NotFoundException.class,
                () -> customersController.removeCustomer(NOT_FOUND_ID));
        verify(customersService).removeCustomer(NOT_FOUND_ID);
    }
}

