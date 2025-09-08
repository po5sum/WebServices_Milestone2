package com.musicstore.musiccatalog.mappinglayer;

import com.musicstore.musiccatalog.dataaccesslayer.artist.Artist;
import com.musicstore.musiccatalog.presentationlayer.artist.ArtistController;
import com.musicstore.musiccatalog.presentationlayer.artist.ArtistResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface ArtistResponseMapper {
    @Mappings({
            @Mapping(expression = "java(artist.getArtistIdentifier().getArtistId())", target = "artistId"),
            @Mapping(expression = "java(artist.getArtistInformation().getArtistName())", target = "artistName"),
            @Mapping(expression = "java(artist.getArtistInformation().getCountry())", target = "country"),
            @Mapping(expression = "java(artist.getArtistInformation().getDebutYear())", target = "debutYear"),
            @Mapping(expression = "java(artist.getArtistInformation().getBiography())", target = "biography")
    })
    ArtistResponseModel entityToResponseModel(Artist artist);

    List<ArtistResponseModel> entityListToResponseModelList(List<Artist> artists);

    @AfterMapping
    default void addLinks(@MappingTarget ArtistResponseModel artistResponseModel) {
        //self link
        Link selfLink = linkTo(methodOn(ArtistController.class).getArtistByArtistId
                (artistResponseModel.getArtistId()))
                .withSelfRel();
        artistResponseModel.add(selfLink);

        //link to all
        Link allArtistsLink = linkTo(methodOn(ArtistController.class)
                .getAllArtists())
                .withRel("artists");
        artistResponseModel.add(allArtistsLink);
    }
}
