package com.musicstore.musiccatalog.dataaccesslayer.album;

import com.musicstore.musiccatalog.dataaccesslayer.artist.ArtistIdentifier;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Table(name = "albums")
@Data
@NoArgsConstructor
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; //private identifier

    @Embedded
    private AlbumIdentifier albumIdentifier;

    @Embedded
    private ArtistIdentifier artistIdentifier;

    @Embedded
    private AlbumInformation albumInformation;

    @Enumerated(EnumType.STRING)
    private AlbumGenreEnum albumGenre;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Album(@NotNull AlbumIdentifier albumIdentifier, @NotNull AlbumInformation albumInformation, @NotNull ArtistIdentifier artistIdentifier, @NotNull AlbumGenreEnum albumGenre, @NotNull Status status) {
        this.albumIdentifier = albumIdentifier;
        this.albumInformation = albumInformation;
        this.artistIdentifier = artistIdentifier;
        this.albumGenre = albumGenre;
        this.status = status;
    }
}
