package com.musicstore.apigateway.storelocation.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreRequestModel {
    String ownerName;
    String managerName;
    Double storeRating;
    String phoneNumber;
    String email;
    String openHours;
    String streetAddress;
    String city;
    String province;
    String postalCode;
}
