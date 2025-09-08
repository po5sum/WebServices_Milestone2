package com.musicstore.apigateway.musiccatalog.businesslayer.album;

import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumRequestModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumResponseModel;

import java.util.List;

public interface AlbumsService {
    List<AlbumResponseModel> getAllAlbums(String artistId);
    AlbumResponseModel getAlbumByAlbumId(String artistId, String albumId);
    AlbumResponseModel addAlbum(AlbumRequestModel albumRequestModel, String artistId);
    AlbumResponseModel updateAlbum(AlbumRequestModel albumRequestModel, String artistId, String albumId);
    void deleteAlbum(String artistId, String albumId);
}
