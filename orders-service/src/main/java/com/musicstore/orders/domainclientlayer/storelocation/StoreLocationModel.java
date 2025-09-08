package com.musicstore.orders.domainclientlayer.storelocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class StoreLocationModel {
    String storeId;
    String ownerName;
    String managerName;
}
