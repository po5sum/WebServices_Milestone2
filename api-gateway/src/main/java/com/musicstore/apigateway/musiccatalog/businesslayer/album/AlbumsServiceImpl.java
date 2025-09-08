package com.musicstore.apigateway.musiccatalog.businesslayer.album;

import com.musicstore.apigateway.musiccatalog.domainclientlayer.MusicCatalogServiceClient;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumRequestModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumResponseModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumsController;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class AlbumsServiceImpl implements AlbumsService {

    private final MusicCatalogServiceClient musicCatalogServiceClient;

    public AlbumsServiceImpl(MusicCatalogServiceClient musicCatalogServiceClient) {
        this.musicCatalogServiceClient = musicCatalogServiceClient;
    }


    @Override
    public List<AlbumResponseModel> getAllAlbums(String artistId) {
        List<AlbumResponseModel> albums = musicCatalogServiceClient.getAllAlbums(artistId);
        if (albums != null) {
            for (AlbumResponseModel album : albums) {
                addLinks(album, artistId);
            }
        }
        return albums;
    }

    @Override
    public AlbumResponseModel getAlbumByAlbumId(String artistId, String albumId) {
        AlbumResponseModel album = musicCatalogServiceClient.getAlbumByAlbumId(artistId, albumId);
        if (album != null) {
            addLinks(album, artistId);
        }
        return album;
    }

    @Override
    public AlbumResponseModel addAlbum(AlbumRequestModel albumRequestModel, String artistId) {
        AlbumResponseModel album = musicCatalogServiceClient.addAlbum(albumRequestModel, artistId);
        if (album != null) {
            addLinks(album, artistId);
        }
        return album;
    }

    @Override
    public AlbumResponseModel updateAlbum(AlbumRequestModel albumRequestModel, String artistId, String albumId) {
        AlbumResponseModel album = musicCatalogServiceClient.updateAlbum(albumRequestModel, artistId, albumId);
        if (album != null) {
            addLinks(album, artistId);
        }
        return album;
    }

    @Override
    public void deleteAlbum(String artistId, String albumId) {
        musicCatalogServiceClient.deleteAlbum(artistId, albumId);
    }

    private AlbumResponseModel addLinks(AlbumResponseModel album, String artistId) {
        Link selfLink = linkTo(methodOn(AlbumsController.class)
                .getAlbumByAlbumId(artistId, album.getAlbumId())).withSelfRel();
        Link allAlbumsLink = linkTo(methodOn(AlbumsController.class)
                .getAllAlbums(artistId)).withRel("albums");

        album.add(selfLink);
        album.add(allAlbumsLink);

        return album;
    }
}
