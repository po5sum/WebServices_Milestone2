package com.musicstore.customers.mappinglayer;


import com.musicstore.customers.dataaccesslayer.Customer;
import com.musicstore.customers.dataaccesslayer.CustomerAddress;
import com.musicstore.customers.dataaccesslayer.CustomerIdentifier;
import com.musicstore.customers.presentationlayer.CustomerRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CustomerRequestMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
    })
    Customer requestModelToEntity(CustomerRequestModel customerRequestModel, CustomerIdentifier customerIdentifier,
                                  CustomerAddress customerAddress);
}
