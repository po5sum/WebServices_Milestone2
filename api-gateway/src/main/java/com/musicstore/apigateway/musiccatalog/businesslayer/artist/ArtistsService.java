package com.musicstore.apigateway.musiccatalog.businesslayer.artist;

import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistRequestModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistResponseModel;

import java.util.List;

public interface ArtistsService {
    List<ArtistResponseModel> getAllArtists();
    ArtistResponseModel getArtistByArtistId(String artistId);
    ArtistResponseModel addArtist(ArtistRequestModel artistRequestModel);
    ArtistResponseModel updateArtist(ArtistRequestModel artistRequestModel, String artistId);
    void deleteArtist(String artistId);
}
