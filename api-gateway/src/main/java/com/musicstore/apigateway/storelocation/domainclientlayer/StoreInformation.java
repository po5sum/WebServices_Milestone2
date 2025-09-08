package com.musicstore.apigateway.storelocation.domainclientlayer;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StoreInformation {

    private String ownerName;
    private String managerName;
    private Double storeRating;
    private String phoneNumber;
    private String email;
    private String openHours;

    public StoreInformation() {
    }

    public StoreInformation(@NotNull String ownerName, @NotNull String managerName, @NotNull Double storeRating, @NotNull String phoneNumber, @NotNull String email, @NotNull String openHours) {
        this.ownerName = ownerName;
        this.managerName = managerName;
        this.storeRating = storeRating;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.openHours = openHours;
    }
}
