package com.musicstore.musiccatalog.dataaccesslayer.artist;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import org.antlr.v4.runtime.misc.NotNull;

@Embeddable
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
