package com.musicstore.apigateway.customers.domainclientlayer;


import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@NoArgsConstructor
@Getter
public class CustomerAddress {

    private String streetAddress;
    private String city;
    private String province;
    private String country;
    private String postalCode;


    public CustomerAddress(@NotNull String streetAddress, @NotNull String city, @NotNull String province, @NotNull String country, @NotNull String postalCode) {

        Objects.requireNonNull(this.streetAddress = streetAddress);
        Objects.requireNonNull(this.city = city);
        Objects.requireNonNull(this.province = province);
        Objects.requireNonNull(this.country = country);
        Objects.requireNonNull(this.postalCode = postalCode);

    }

}
