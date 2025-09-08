package com.musicstore.customers.presentationlayer;

import com.musicstore.customers.dataaccesslayer.*;
import com.musicstore.customers.utils.exceptions.DuplicateEmailException;
import com.musicstore.customers.utils.exceptions.InvalidInputException;
import com.musicstore.customers.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data-h2.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CustomerControllerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CustomerRepository customerRepository;

    private final String BASE_URL_CUSTOMERS = "/api/v1/customers";
    private final String VALID_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String NOT_FOUND_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d7";
    private final String INVALID_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708d";

    @Test
    public void whenCustomerExists_thenReturnAllCustomers() {
        //arrange
        long sizeDb = customerRepository.count();

        //act and assert
        webTestClient.get().uri(BASE_URL_CUSTOMERS).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBodyList(CustomerResponseModel.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertNotEquals(0, list.size());
                    assertEquals(sizeDb, list.size());
                });
    }

    @Test
    public void whenCustomerRequestIsValid_thenReturnNewCustomer() {
        //arrange
        PhoneNumber phoneNumber1 = new PhoneNumber(PhoneType.MOBILE, "666-555-5555");
        PhoneNumber phoneNumber2 = new PhoneNumber(PhoneType.HOME, "777-555-2222");
        List<PhoneNumber> numbers = new ArrayList<>(Arrays.asList(phoneNumber1, phoneNumber2));

        CustomerRequestModel customerRequestModel = CustomerRequestModel.builder()
                .firstName("John")
                .lastName("Doe")
                .emailAddress("john@doe.com")
                .contactMethodPreference(ContactMethodPreference.EMAIL)
                .streetAddress("123 main street")
                .city("London")
                .province("London")
                .country("uk")
                .postalCode("12345")
                .phoneNumbers(numbers)
                .build();

        //act and assert
        webTestClient.post().uri(BASE_URL_CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .bodyValue(customerRequestModel).exchange().expectStatus().isCreated().expectHeader()
                .contentType(MediaType.APPLICATION_JSON).expectBody(CustomerResponseModel.class)
                .value((customerResponseModel) -> {
                    assertNotNull(customerResponseModel);
                    assertNotNull(customerResponseModel.getCustomerId());
                    assertNotNull(customerRequestModel.getFirstName(), customerResponseModel.getFirstName());
                    //test every fields
                });
    }

    @Test
    public void whenCustomerEmailAlreadyExists_thenReturnUnprocessableEntity() {
        CustomerRequestModel duplicateEmailRequest = CustomerRequestModel.builder()
                .firstName("Dup")
                .lastName("User")
                .emailAddress("aucceli0@dot.gov") // existing email
                .contactMethodPreference(ContactMethodPreference.EMAIL)
                .streetAddress("456 Dup St")
                .city("DupCity")
                .province("DupProv")
                .country("DupCountry")
                .postalCode("99999")
                .phoneNumbers(new ArrayList<>())
                .build();

        webTestClient.post().uri(BASE_URL_CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(duplicateEmailRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .value(msg -> assertTrue(((String) msg).toLowerCase().contains("already exists")));
    }



    @Test
    public void whenCustomerExistsOnDelete_thenReturnNoCustomer() {
        webTestClient.delete().uri(BASE_URL_CUSTOMERS + "/" + VALID_CUSTOMER_ID)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNoContent();

        webTestClient.get().uri(BASE_URL_CUSTOMERS + "/" + VALID_CUSTOMER_ID)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$.message")
                .isEqualTo("Provided customerId not found: " + VALID_CUSTOMER_ID);  // Update if needed to match backend
    }

    @Test
    public void whenCustomerIdIsInvalidOnDelete_thenReturnUnprocessableEntity() {
        webTestClient.delete().uri(BASE_URL_CUSTOMERS + "/" + INVALID_CUSTOMER_ID)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody().jsonPath("$.message")
                .isEqualTo("Invalid customerId provided: " + INVALID_CUSTOMER_ID);
        // Updated to match actual message
    }

    @Test
    public void whenCustomerDoesNotExistOnDelete_thenReturnNotFound() {
        webTestClient.delete().uri(BASE_URL_CUSTOMERS + "/" + NOT_FOUND_CUSTOMER_ID)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound()
                .expectBody().jsonPath("$.message")
                .isEqualTo("Provided customerId not found: " + NOT_FOUND_CUSTOMER_ID);
    }

    @Test
    public void whenCustomerIdIsInvalidOnGet_thenReturnUnprocessableEntity() {
        webTestClient.get().uri(BASE_URL_CUSTOMERS + "/" + INVALID_CUSTOMER_ID)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody().jsonPath("$.message")
                .isEqualTo("Invalid customerId provided: " + INVALID_CUSTOMER_ID);
    }

    @Test
    public void whenCustomerDoesNotExistOnGet_thenReturnNotFound() {
        webTestClient.get().uri(BASE_URL_CUSTOMERS + "/" + NOT_FOUND_CUSTOMER_ID)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound()
                .expectBody().jsonPath("$.message")
                .isEqualTo("Provided customerId not found: " + NOT_FOUND_CUSTOMER_ID);
    }

    @Test
    public void whenCustomerExistsOnGet_thenReturnCustomer() {
        webTestClient.get().uri(BASE_URL_CUSTOMERS + "/" + VALID_CUSTOMER_ID)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value((customerResponseModel) -> {
                    assertNotNull(customerResponseModel);
                    assertEquals(VALID_CUSTOMER_ID, customerResponseModel.getCustomerId());
                    //test every fields
                });
    }


    @Test
    public void whenCustomerIdIsInvalidOnUpdate_thenReturnUnprocessableEntity() {
        CustomerRequestModel updateRequest = CustomerRequestModel.builder()
                .firstName("UpdatedName")
                .lastName("UpdatedLastName")
                .emailAddress("updated.email@example.com")
                .build();

        webTestClient.put().uri(BASE_URL_CUSTOMERS + "/" + INVALID_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest).exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody().jsonPath("$.message")
                .isEqualTo("Invalid customerId provided: " + INVALID_CUSTOMER_ID);
    }

    @Test
    public void whenCustomerExistsOnUpdate_thenReturnUpdatedCustomer() {
        // Arrange
        CustomerRequestModel updateRequest = CustomerRequestModel.builder()
                .firstName("UpdatedName")
                .lastName("UpdatedLastName")
                .emailAddress("updated.email@example.com")
                .streetAddress("456 Updated Street")
                .city("Updated City")
                .province("Updated Province")
                .country("Updated Country")
                .postalCode("67890")
                .build();

        // Act and Assert
        webTestClient.put().uri(BASE_URL_CUSTOMERS + "/" + VALID_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest).exchange().expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertEquals("UpdatedName", response.getFirstName());
                    assertEquals("UpdatedLastName", response.getLastName());
                    assertEquals("updated.email@example.com", response.getEmailAddress());
                    assertEquals("456 Updated Street", response.getStreetAddress());
                });
    }

    @Test
    public void whenCustomerDoesNotExistOnUpdate_thenReturnNotFound() {
        // Arrange
        CustomerRequestModel updateRequest = CustomerRequestModel.builder()
                .firstName("UpdatedName")
                .lastName("UpdatedLastName")
                .emailAddress("updated.email@example.com")
                .build();

        // Act and Assert
        webTestClient.put().uri(BASE_URL_CUSTOMERS + "/" + NOT_FOUND_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest).exchange().expectStatus().isNotFound()
                .expectBody().jsonPath("$.message")
                .isEqualTo("Provided customerId not found: " + NOT_FOUND_CUSTOMER_ID);
    }
    @Test
    public void testCustomerIdentifierConstructorAndGetter() {
        String uuid = "test-1234";
        CustomerIdentifier id = new CustomerIdentifier(uuid);
        assertEquals(uuid, id.getCustomerId(), "CustomerIdentifier#getCustomerId must return the ctor value");
    }

    @Test
    public void testDuplicateEmailExceptionConstructors() {
        // no-arg
        DuplicateEmailException ex1 = new DuplicateEmailException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        // message-only
        String msg = "dup email";
        DuplicateEmailException ex2 = new DuplicateEmailException(msg);
        assertEquals(msg, ex2.getMessage());

        // cause-only
        RuntimeException cause = new RuntimeException("boom");
        DuplicateEmailException ex3 = new DuplicateEmailException(cause);
        assertSame(cause, ex3.getCause());

        // message + cause
        String msg2 = "dup2";
        RuntimeException cause2 = new RuntimeException("kaboom");
        DuplicateEmailException ex4 = new DuplicateEmailException(msg2, cause2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(cause2, ex4.getCause());
    }

    @Test
    public void testInvalidInputExceptionConstructors() {
        // no-arg
        InvalidInputException ex1 = new InvalidInputException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        // message-only
        String msg = "Invalid input";
        InvalidInputException ex2 = new InvalidInputException(msg);
        assertEquals(msg, ex2.getMessage());

        // cause-only
        RuntimeException cause = new RuntimeException("cause");
        InvalidInputException ex3 = new InvalidInputException(cause);
        assertSame(cause, ex3.getCause());

        // message + cause
        String msg2 = "Invalid input with cause";
        RuntimeException cause2 = new RuntimeException("cause2");
        InvalidInputException ex4 = new InvalidInputException(msg2, cause2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(cause2, ex4.getCause());
    }

    @Test
    public void testNotFoundExceptionConstructors() {
        // no-arg
        NotFoundException ex1 = new NotFoundException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        // message-only
        String msg = "Not found";
        NotFoundException ex2 = new NotFoundException(msg);
        assertEquals(msg, ex2.getMessage());

        // cause-only
        RuntimeException cause = new RuntimeException("cause");
        NotFoundException ex3 = new NotFoundException(cause);
        assertSame(cause, ex3.getCause());

        // message + cause
        String msg2 = "Not found with cause";
        RuntimeException cause2 = new RuntimeException("cause2");
        NotFoundException ex4 = new NotFoundException(msg2, cause2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(cause2, ex4.getCause());
    }
}

