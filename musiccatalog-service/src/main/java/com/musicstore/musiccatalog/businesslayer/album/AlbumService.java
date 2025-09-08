package com.musicstore.musiccatalog.businesslayer.album;

import com.musicstore.musiccatalog.dataaccesslayer.album.Status;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumRequestModel;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumResponseModel;

import java.util.List;
import java.util.Map;

public interface AlbumService {
    List<AlbumResponseModel> getAllAlbums(String artistId, Map<String, String> queryParams);
    AlbumResponseModel getAlbumByAlbumId(String albumId);
    AlbumResponseModel addAlbum(AlbumRequestModel albumRequestModel, String artistId);
    AlbumResponseModel updateAlbum(AlbumRequestModel albumRequestModel, String artistId, String albumId);
    void deleteAlbum(String artistId, String albumId);

    AlbumResponseModel updateCondition(String artistId, String albumId, Status newCondition);
}
