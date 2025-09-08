package com.musicstore.musiccatalog.dataaccesslayer.artist;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, String> {
    Artist findByArtistIdentifier_ArtistId(String artistId);
    boolean existsByArtistInformation_ArtistName(String artistName);
}
