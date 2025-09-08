package com.musicstore.musiccatalog.presentationlayer.album;

import com.musicstore.musiccatalog.dataaccesslayer.album.AlbumGenreEnum;
import com.musicstore.musiccatalog.dataaccesslayer.album.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumRequestModel {
    String artistId;
    String albumTitle;
    Integer releaseDate;
    String albumLength;
    AlbumGenreEnum albumGenre;
    Status status;
}
