package com.musicstore.apigateway.customers.presentationlayer;

import com.musicstore.apigateway.customers.domainclientlayer.ContactMethodPreference;
import com.musicstore.apigateway.customers.domainclientlayer.PhoneNumber;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequestModel {
    String firstName;
    String lastName;
    String emailAddress;
    ContactMethodPreference contactMethodPreference;
    String streetAddress;
    String city;
    String province;
    String country;
    String postalCode;
    List<PhoneNumber> phoneNumbers;
}