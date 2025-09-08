package com.musicstore.customers.dataaccesslayer;

import com.musicstore.customers.utils.exceptions.DuplicateEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CustomerRepositoryIntegrationTest {
    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUpDB() {
        customerRepository.deleteAll();
    }

    @Test
    public void whenCustomerExists_thenReturnAllCustomers() {
        // arrange
        Customer customer1 = new Customer("John", "Pork", "john.pork@gmail.com", ContactMethodPreference.EMAIL,
                new CustomerAddress("123 Main", "St. Johns", "Newfoundland", "Canada", "H1H1H1"),
                new ArrayList<>(Arrays.asList(
                        new PhoneNumber(PhoneType.MOBILE, "555-555-5555"),
                        new PhoneNumber(PhoneType.HOME, "444-555-5555")
                ))
        );

        Customer customer2 = new Customer("Tim", "Cheese", "tim.cheese@gmail.com", ContactMethodPreference.EMAIL,
                new CustomerAddress("123 Main", "St. Johns", "Newfoundland", "Canada", "H1H1H1"),
                new ArrayList<>(Arrays.asList(
                        new PhoneNumber(PhoneType.MOBILE, "666-555-5555"),
                        new PhoneNumber(PhoneType.HOME, "111-555-5555")
                ))
        );
        customerRepository.save(customer1);
        customerRepository.save(customer2);
        long afterSizeDB = customerRepository.count();

        // act
        List<Customer> customerList = customerRepository.findAll();

        // assert
        assertNotNull(customerList);
        assertNotEquals(0, afterSizeDB);
        assertEquals(afterSizeDB, customerList.size());
    }

    @Test
    public void whenCustomerDoesNotExist_thenReturnCustomerByCustomerId() {
        // arrange
        Customer customer1 = new Customer("John", "Pork", "john.pork@gmail.com", ContactMethodPreference.EMAIL,
                new CustomerAddress("123 Main", "St. Johns", "Newfoundland", "Canada", "H1H1H1"),
                new ArrayList<>(Arrays.asList(
                        new PhoneNumber(PhoneType.MOBILE, "555-555-5555"),
                        new PhoneNumber(PhoneType.HOME, "444-555-5555")
                ))
        );

        Customer customer2 = new Customer("Tim", "Cheese", "tim.cheese@gmail.com", ContactMethodPreference.EMAIL,
                new CustomerAddress("123 Main", "St. Johns", "Newfoundland", "Canada", "H1H1H1"),
                new ArrayList<>(Arrays.asList(
                        new PhoneNumber(PhoneType.MOBILE, "666-555-5555"),
                        new PhoneNumber(PhoneType.HOME, "111-555-5555")
                ))
        );
        customerRepository.save(customer1);
        customerRepository.save(customer2);

        // act
        Customer foundCustomer = customerRepository.findByCustomerIdentifier_CustomerId(
                customer1.getCustomerIdentifier().getCustomerId()
        );

        // assert
        assertNotNull(foundCustomer);
        assertEquals(customer1.getCustomerIdentifier().getCustomerId(), foundCustomer.getCustomerIdentifier().getCustomerId());
        assertEquals(customer1.getFirstName(), foundCustomer.getFirstName());
        assertIterableEquals(customer1.getPhoneNumbers(), foundCustomer.getPhoneNumbers());
    }

    @Test
    public void whenCustomerDoesNotExist_thenReturnNull(){
        // arrange
        final String NOT_FOUND_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";

        // act
        Customer foundCustomer = customerRepository.findByCustomerIdentifier_CustomerId(NOT_FOUND_CUSTOMER_ID);

        // assert
        assertNull(foundCustomer);
    }

    @Test
    public void whenCustomerEntityIsValid_thenAddCustomer(){
        // arrange
        PhoneNumber phoneNumber1 = new PhoneNumber(PhoneType.MOBILE, "666-555-5555");
        PhoneNumber phoneNumber2 = new PhoneNumber(PhoneType.HOME, "777-555-2222");
        List<PhoneNumber> numbers = new ArrayList<>(Arrays.asList(phoneNumber1, phoneNumber2));

        Customer customer = new Customer("John", "Pork", "john.pork@gmail.com",
                ContactMethodPreference.EMAIL,
                new CustomerAddress("123 Main", "St. Johns", "Newfoundland", "Canada", "H1H1H1"),
                numbers
        );

        // act
        Customer savedCustomer = customerRepository.save(customer);

        // assert
        assertNotNull(savedCustomer);
        assertNotNull(savedCustomer.getId());
        assertNotNull(savedCustomer.getCustomerIdentifier());
        assertNotNull(savedCustomer.getCustomerIdentifier().getCustomerId());
        assertEquals(customer.getFirstName(), savedCustomer.getFirstName());
        assertEquals(customer.getLastName(), savedCustomer.getLastName());
        assertEquals(customer.getEmailAddress(), savedCustomer.getEmailAddress());
        assertEquals(customer.getContactMethodPreference(), savedCustomer.getContactMethodPreference());
        assertEquals(customer.getPhoneNumbers(), savedCustomer.getPhoneNumbers());
        assertEquals(customer.getCustomerAddress(), savedCustomer.getCustomerAddress());
        assertIterableEquals(customer.getPhoneNumbers(), savedCustomer.getPhoneNumbers());
        assertEquals(customer.getPhoneNumbers().size(), savedCustomer.getPhoneNumbers().size());
    }


    @Test
    public void whenCustomerEntityIsUpdated_thenReturnUpdatedCustomer(){
        // arrange
        PhoneNumber phoneNumber = new PhoneNumber(PhoneType.MOBILE, "123-456-7890");
        List<PhoneNumber> numbers = new ArrayList<>(Arrays.asList(phoneNumber));
        Customer customer = new Customer("Alice", "Wonderland", "alice@gmail.com",
                ContactMethodPreference.EMAIL,
                new CustomerAddress("456 Main", "St. Johns", "Newfoundland", "Canada", "A1A1A1"),
                numbers
        );
        Customer savedCustomer = customerRepository.save(customer);

        // update fields
        savedCustomer.setEmailAddress("alice.updated@gmail.com");
        savedCustomer.setLastName("Liddell");

        // act: update the customer in the DB
        Customer updatedCustomer = customerRepository.save(savedCustomer);
        Customer foundCustomer = customerRepository.findByCustomerIdentifier_CustomerId(
                updatedCustomer.getCustomerIdentifier().getCustomerId()
        );

        // assert
        assertNotNull(foundCustomer);
        assertEquals("alice.updated@gmail.com", foundCustomer.getEmailAddress());
        assertEquals("Liddell", foundCustomer.getLastName());
    }

    @Test
    public void whenUpdateNonExistentCustomer_thenCreateNewRecord() {
        Customer ghost = new Customer("Ghost", "Update", "ghost@void.com",
                ContactMethodPreference.EMAIL,
                new CustomerAddress("Nowhere", "Ghosttown", "Empty", "Canada", "00000"),
                new ArrayList<>());

        long countBefore = customerRepository.count();

        Customer saved = customerRepository.save(ghost);

        assertNotNull(saved);
        assertNotNull(saved.getId()); // should be assigned
        assertEquals(countBefore + 1, customerRepository.count()); // confirms new insert
    }


    @Test
    public void whenCustomerEntityIsDeleted_thenReturnNull(){
        // arrange
        PhoneNumber phoneNumber = new PhoneNumber(PhoneType.MOBILE, "987-654-3210");
        List<PhoneNumber> numbers = new ArrayList<>(Arrays.asList(phoneNumber));
        Customer customer = new Customer("Bob", "Builder", "bob.builder@gmail.com",
                ContactMethodPreference.PHONE,
                new CustomerAddress("789 Main", "St. Johns", "Newfoundland", "Canada", "B2B2B2"),
                numbers
        );
        Customer savedCustomer = customerRepository.save(customer);

        // act: delete the customer
        customerRepository.delete(savedCustomer);

        // assert: attempt to find the deleted customer should return null
        Customer deletedCustomer = customerRepository.findByCustomerIdentifier_CustomerId(
                savedCustomer.getCustomerIdentifier().getCustomerId()
        );
        assertNull(deletedCustomer);
    }

    @Test
    public void whenDeleteNonExistentCustomer_thenNoExceptionThrown() {
        Customer ghost = new Customer("Ghost", "Delete", "ghost@delete.com",
                ContactMethodPreference.EMAIL,
                new CustomerAddress("Nowhere", "Ghosttown", "Empty", "Canada", "00000"),
                new ArrayList<>());

        assertDoesNotThrow(() -> customerRepository.delete(ghost));
    }


    @Test
    public void whenCustomerExistsByEmail_thenReturnTrue() {
        Customer customer = new Customer("Jane", "Doe", "jane.doe@example.com", ContactMethodPreference.EMAIL,
                new CustomerAddress("123 Main", "City", "Province", "Country", "12345"),
                new ArrayList<>(Arrays.asList(new PhoneNumber(PhoneType.MOBILE, "123-456-7890")))
        );
        customerRepository.save(customer);

        boolean exists = customerRepository.existsByEmailAddress("jane.doe@example.com");

        assertTrue(exists);
    }

    @Test
    public void whenCustomerDoesNotExistByEmail_thenReturnFalse() {
        boolean exists = customerRepository.existsByEmailAddress("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    public void whenFindAllCustomers_thenReturnEmptyListIfNoCustomersExist() {
        customerRepository.deleteAll();

        List<Customer> customers = customerRepository.findAll();

        assertNotNull(customers);
        assertTrue(customers.isEmpty());
    }
    @Test
    public void testCustomerIdentifierConstructorAndGetter() {
        String uuid = "test-1234";
        CustomerIdentifier id = new CustomerIdentifier(uuid);
        assertEquals(uuid, id.getCustomerId(), "CustomerIdentifier#getCustomerId must return the ctor value");
    }
    @Test
    public void whenCustomerConstructorIsCalled_thenAllFieldsAreInitialized() {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";
        String emailAddress = "john.doe@example.com";
        ContactMethodPreference contactMethodPreference = ContactMethodPreference.EMAIL;
        CustomerAddress customerAddress = new CustomerAddress("123 Main St", "City", "Province", "Country", "12345");
        List<PhoneNumber> phoneNumbers = new ArrayList<>(Arrays.asList(
                new PhoneNumber(PhoneType.MOBILE, "123-456-7890"),
                new PhoneNumber(PhoneType.HOME, "987-654-3210")
        ));

        // Act
        Customer customer = new Customer(firstName, lastName, emailAddress, contactMethodPreference, customerAddress, phoneNumbers);

        // Assert
        assertNotNull(customer.getCustomerIdentifier());
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
        assertEquals(emailAddress, customer.getEmailAddress());
        assertEquals(contactMethodPreference, customer.getContactMethodPreference());
        assertEquals(customerAddress, customer.getCustomerAddress());
        assertEquals(phoneNumbers, customer.getPhoneNumbers());
    }
}

