package com.musicstore.musiccatalog.businesslayer.artist;


import com.musicstore.musiccatalog.presentationlayer.artist.ArtistRequestModel;
import com.musicstore.musiccatalog.presentationlayer.artist.ArtistResponseModel;

import java.util.List;

public interface ArtistService {
    List <ArtistResponseModel> getAllArtists();
    ArtistResponseModel getArtistByArtistId(String artistId);
    ArtistResponseModel addArtist(ArtistRequestModel artistRequestModel);
    ArtistResponseModel updateArtist(ArtistRequestModel artistRequestModel, String artistId);
    void deleteArtist(String artistId);
}
