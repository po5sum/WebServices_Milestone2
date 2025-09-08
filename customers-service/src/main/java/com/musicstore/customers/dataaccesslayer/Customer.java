package com.musicstore.customers.dataaccesslayer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;


import java.util.List;

@Entity
@Table(name="customers")
@Data
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; //private identifier

    @Embedded
    private CustomerIdentifier customerIdentifier; //public identifier

    private String firstName;
    private String lastName;
    private String emailAddress;

    @Enumerated(EnumType.STRING)
    private ContactMethodPreference contactMethodPreference;

    @Embedded
    private CustomerAddress customerAddress;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "customer_phonenumbers", joinColumns = @JoinColumn(name="customer_id"))
    private List<PhoneNumber> phoneNumbers;

    public Customer(@NotNull String firstName, @NotNull String lastName, @NotNull String emailAddress, @NotNull ContactMethodPreference contactMethodPreference, @NotNull CustomerAddress customerAddress,
                    @NotNull List<PhoneNumber> phoneNumberList) {
        this.customerIdentifier = new CustomerIdentifier();
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.contactMethodPreference = contactMethodPreference;
        this.customerAddress = customerAddress;
        this.phoneNumbers = phoneNumberList;
    }
}
