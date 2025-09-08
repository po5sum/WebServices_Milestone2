package com.musicstore.musiccatalog.dataaccesslayer.album;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, String> {
    List<Album> findAllByArtistIdentifier_ArtistId(String artistId);
    Album findByAlbumIdentifier_AlbumId(String albumId);
    Album findAlbumByArtistIdentifier_ArtistIdAndAlbumIdentifier_AlbumId(String artistId, String albumId);
    List<Album> findAlbumByArtistIdentifier_ArtistIdAndAlbumGenre(String artistId, AlbumGenreEnum albumGenre);
}
