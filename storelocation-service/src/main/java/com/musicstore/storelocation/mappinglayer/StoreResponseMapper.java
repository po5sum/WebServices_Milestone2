package com.musicstore.storelocation.mappinglayer;

import com.musicstore.storelocation.dataaccesslayer.Store;
import com.musicstore.storelocation.presentationlayer.StoreController;
import com.musicstore.storelocation.presentationlayer.StoreResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface StoreResponseMapper {

    @Mappings({
            @Mapping(expression = "java(store.getStoreIdentifier().getStoreId())", target = "storeId"),
            @Mapping(expression = "java(store.getStoreInformation().getOwnerName())", target = "ownerName"),
            @Mapping(expression = "java(store.getStoreInformation().getManagerName())", target = "managerName"),
            @Mapping(expression = "java(store.getStoreInformation().getStoreRating())", target = "storeRating"),
            @Mapping(expression = "java(store.getStoreInformation().getPhoneNumber())", target = "phoneNumber"),
            @Mapping(expression = "java(store.getStoreInformation().getEmail())", target = "email"),
            @Mapping(expression = "java(store.getStoreInformation().getOpenHours())", target = "openHours"),
            @Mapping(expression = "java(store.getStoreAddress().getStreetAddress())", target = "streetAddress"),
            @Mapping(expression = "java(store.getStoreAddress().getCity())", target = "city"),
            @Mapping(expression = "java(store.getStoreAddress().getProvince())", target = "province"),
            @Mapping(expression = "java(store.getStoreAddress().getPostalCode())", target = "postalCode")
    })
    StoreResponseModel entityToResponseModel(Store store);

    List<StoreResponseModel> entityListToResponseModelList(List<Store> stores);

    @AfterMapping
    default void addLinks(@MappingTarget StoreResponseModel storeResponseModel) {
        Link selfLink = linkTo(methodOn(StoreController.class)
                .getStoreByStoreId(storeResponseModel.getStoreId())).withSelfRel();
        storeResponseModel.add(selfLink);

        Link allStoresLink = linkTo(methodOn(StoreController.class)
                .getAllStores()).withRel("stores");
        storeResponseModel.add(allStoresLink);
    }
}
