package com.musicstore.apigateway.musiccatalog.businesslayer.artist;

import com.musicstore.apigateway.musiccatalog.domainclientlayer.MusicCatalogServiceClient;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistRequestModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistResponseModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistsController;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ArtistsServiceImpl implements ArtistsService {

    private final MusicCatalogServiceClient musicCatalogServiceClient;

    public ArtistsServiceImpl(MusicCatalogServiceClient musicCatalogServiceClient) {
        this.musicCatalogServiceClient = musicCatalogServiceClient;
    }

    @Override
    public List<ArtistResponseModel> getAllArtists() {
        List<ArtistResponseModel> artists = musicCatalogServiceClient.getAllArtists();
        if (artists != null) {
            for (ArtistResponseModel artist : artists) {
                addLinks(artist);
            }
        }
        return artists;
    }

    @Override
    public ArtistResponseModel getArtistByArtistId(String artistId) {
        ArtistResponseModel artist = musicCatalogServiceClient.getArtistByArtistId(artistId);
        if (artist != null) {
            addLinks(artist);
        }
        return artist;
    }

    @Override
    public ArtistResponseModel addArtist(ArtistRequestModel artistRequestModel) {
        ArtistResponseModel artist = musicCatalogServiceClient.addArtist(artistRequestModel);
        if (artist != null) {
            addLinks(artist);
        }
        return artist;
    }

    @Override
    public ArtistResponseModel updateArtist(ArtistRequestModel artistRequestModel, String artistId) {
        ArtistResponseModel artist = musicCatalogServiceClient.updateArtist(artistRequestModel, artistId);
        if (artist != null) {
            addLinks(artist);
        }
        return artist;
    }

    @Override
    public void deleteArtist(String artistId) {
        musicCatalogServiceClient.deleteArtist(artistId);
    }

    private ArtistResponseModel addLinks(ArtistResponseModel artist) {
        Link selfLink = linkTo(methodOn(ArtistsController.class)
                .getArtistById(artist.getArtistId())).withSelfRel();
        Link allArtistsLink = linkTo(methodOn(ArtistsController.class)
                .getAllArtists()).withRel("artists");

        artist.add(selfLink);
        artist.add(allArtistsLink);

        return artist;
    }
}
