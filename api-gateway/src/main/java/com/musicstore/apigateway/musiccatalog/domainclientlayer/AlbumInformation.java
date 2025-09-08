package com.musicstore.apigateway.musiccatalog.domainclientlayer;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;



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
