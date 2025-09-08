package com.musicstore.musiccatalog.dataaccesslayer.album;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import org.antlr.v4.runtime.misc.NotNull;

@Embeddable
@Getter
public class AlbumInformation {
    private String albumTitle;
    private int releaseDate;
    private String albumLength;

    public AlbumInformation() {
    }

    public AlbumInformation(@NotNull String albumTitle, @NotNull int releaseDate, @NotNull String albumLength) {
        this.albumTitle = albumTitle;
        this.releaseDate = releaseDate;
        this.albumLength = albumLength;
    }
}
