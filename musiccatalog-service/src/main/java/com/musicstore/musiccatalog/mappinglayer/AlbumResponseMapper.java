package com.musicstore.musiccatalog.mappinglayer;

import com.musicstore.musiccatalog.dataaccesslayer.album.Album;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumController;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.HashMap;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface AlbumResponseMapper {

    @Mappings({
            @Mapping(expression = "java(album.getAlbumIdentifier().getAlbumId())", target = "albumId"), // Mapping album ID from albumIdentifier
            @Mapping(expression = "java(album.getArtistIdentifier().getArtistId())", target = "artistId"),
            @Mapping(expression = "java(album.getAlbumInformation().getAlbumTitle())", target = "albumTitle"), // Mapping album title from albumInformation
            @Mapping(expression = "java(album.getAlbumInformation().getReleaseDate())", target = "releaseDate"), // Mapping release date from albumInformation
            @Mapping(expression = "java(album.getAlbumInformation().getAlbumLength())", target = "albumLength"),
            @Mapping(expression = "java(album.getAlbumGenre())", target = "albumGenre"),
            @Mapping(expression = "java(album.getStatus())", target = "status") // Mapping album genre
    })
    AlbumResponseModel entityToResponseModel(Album album);

    List<AlbumResponseModel> entityListToResponseModelList(List<Album> albums);

    @AfterMapping
    default void addLinks(@MappingTarget AlbumResponseModel albumResponseModel) {
        //self link
        Link selfLink = linkTo(methodOn(AlbumController.class)
                .getAlbumByAlbumId(albumResponseModel.getAlbumId()))
                .withSelfRel();
        albumResponseModel.add(selfLink);

        Link allAlbumsLink = linkTo(methodOn(AlbumController.class).getAllAlbums(albumResponseModel.getArtistId(), new HashMap<>())).withRel("albums");
        albumResponseModel.add(allAlbumsLink);
    }
}