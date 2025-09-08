package com.musicstore.musiccatalog.dataaccesslayer.artist;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
public class ArtistIdentifier {
    private String artistId;

    public ArtistIdentifier() { this.artistId = UUID.randomUUID().toString(); }

    public ArtistIdentifier(String artistId) {
        this.artistId = artistId;
    }
}
