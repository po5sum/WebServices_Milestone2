package com.musicstore.musiccatalog.dataaccesslayer.album;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
public class AlbumIdentifier {
    private String albumId;

    public AlbumIdentifier() { this.albumId = UUID.randomUUID().toString();}

    public AlbumIdentifier(String albumId) {
        this.albumId = albumId;
    }
}
