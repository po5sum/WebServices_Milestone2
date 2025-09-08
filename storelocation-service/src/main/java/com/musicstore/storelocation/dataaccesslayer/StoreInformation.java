package com.musicstore.storelocation.dataaccesslayer;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import org.antlr.v4.runtime.misc.NotNull;

@Embeddable
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
