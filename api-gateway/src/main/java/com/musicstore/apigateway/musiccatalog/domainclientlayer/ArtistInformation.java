package com.musicstore.apigateway.musiccatalog.domainclientlayer;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;



@Getter
public class ArtistInformation {
    private String artistName;
    private String country;
    private Integer debutYear;
    private String biography;

    public ArtistInformation() {
    }

    public ArtistInformation(@NotNull String artistName, @NotNull String country, @NotNull Integer debutYear, @NotNull String biography) {
        this.artistName = artistName;
        this.country = country;
        this.debutYear = debutYear;
        this.biography = biography;
    }
}
