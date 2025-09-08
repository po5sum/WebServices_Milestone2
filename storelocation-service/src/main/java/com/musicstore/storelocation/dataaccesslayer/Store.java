package com.musicstore.storelocation.dataaccesslayer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; //private identifier

    @Embedded
    private StoreIdentifier storeIdentifier; //store id in response

    @Embedded
    private StoreInformation storeInformation;

    @Embedded
    private StoreAddress storeAddress;

    public Store(@NotNull StoreInformation storeInformation, @NotNull StoreAddress storeAddress) {
        this.storeIdentifier = new StoreIdentifier();
        this.storeInformation = storeInformation;
        this.storeAddress = storeAddress;
    }
}
