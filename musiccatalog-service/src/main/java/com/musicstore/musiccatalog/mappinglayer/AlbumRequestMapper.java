package com.musicstore.musiccatalog.mappinglayer;

import com.musicstore.musiccatalog.dataaccesslayer.album.Album;
import com.musicstore.musiccatalog.dataaccesslayer.album.AlbumIdentifier;
import com.musicstore.musiccatalog.dataaccesslayer.album.AlbumInformation;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AlbumRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),  // ID is auto-generated, so ignore it
    })
    Album requestModelToEntity(AlbumRequestModel requestModel, AlbumIdentifier albumIdentifier, AlbumInformation albumInformation);
}