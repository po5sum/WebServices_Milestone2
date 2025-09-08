package com.musicstore.apigateway.storelocation.domainclientlayer;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StoreAddress {

    private String streetAddress;
    private String city;
    private String province;
    private String postalCode;

    public StoreAddress() {
    }

    public StoreAddress(@NotNull String streetAddress, @NotNull String city, @NotNull String province, @NotNull String postalCode) {
        this.streetAddress = streetAddress;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
    }
}
