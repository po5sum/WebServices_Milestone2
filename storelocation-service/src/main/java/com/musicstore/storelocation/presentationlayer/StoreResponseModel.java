package com.musicstore.storelocation.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreResponseModel extends RepresentationModel<StoreResponseModel> {
    String storeId;
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
