package com.musicstore.musiccatalog.dataaccesslayer.artist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Table(name = "artists")
@Data
@NoArgsConstructor
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; //private identifier

    @Embedded
    private ArtistIdentifier artistIdentifier;

    @Embedded
    private ArtistInformation artistInformation;



    public Artist(@NotNull ArtistInformation artistInformation) {
        this.artistIdentifier = new ArtistIdentifier();
        this.artistInformation = artistInformation;
    }
}
