package com.musicstore.customers.dataaccesslayer;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.Objects;


@Embeddable
@Getter
public class CustomerAddress {

    private String streetAddress;
    private String city;
    private String province;
    private String country;
    private String postalCode;

    @SuppressWarnings("unused")
    public CustomerAddress() {
    }

    public CustomerAddress(@NotNull String streetAddress, @NotNull String city, @NotNull String province, @NotNull String country, @NotNull String postalCode) {

        Objects.requireNonNull(this.streetAddress = streetAddress);
        Objects.requireNonNull(this.city = city);
        Objects.requireNonNull(this.province = province);
        Objects.requireNonNull(this.country = country);
        Objects.requireNonNull(this.postalCode = postalCode);

    }

}
