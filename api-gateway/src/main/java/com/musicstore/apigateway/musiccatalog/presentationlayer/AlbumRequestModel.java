package com.musicstore.apigateway.musiccatalog.presentationlayer;

import com.musicstore.apigateway.musiccatalog.domainclientlayer.AlbumGenreEnum;
import com.musicstore.apigateway.musiccatalog.domainclientlayer.Status;
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
