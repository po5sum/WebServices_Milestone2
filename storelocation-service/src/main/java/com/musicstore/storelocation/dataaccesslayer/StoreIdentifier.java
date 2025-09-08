package com.musicstore.storelocation.dataaccesslayer;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
public class StoreIdentifier {

    private String storeId;

    public StoreIdentifier() { this.storeId = UUID.randomUUID().toString(); }

    public StoreIdentifier(String storeId) { this.storeId = storeId; }
}
