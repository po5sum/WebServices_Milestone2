package com.musicstore.storelocation.mappinglayer;

import com.musicstore.storelocation.dataaccesslayer.Store;
import com.musicstore.storelocation.dataaccesslayer.StoreAddress;
import com.musicstore.storelocation.dataaccesslayer.StoreIdentifier;
import com.musicstore.storelocation.dataaccesslayer.StoreInformation;
import com.musicstore.storelocation.presentationlayer.StoreRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface StoreRequestMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    Store requestModelToEntity(StoreRequestModel storeRequestModel, StoreIdentifier storeIdentifier,
                               StoreInformation storeInformation, StoreAddress storeAddress);
}
