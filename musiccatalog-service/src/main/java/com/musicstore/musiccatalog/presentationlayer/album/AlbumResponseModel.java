package com.musicstore.musiccatalog.presentationlayer.album;

import com.musicstore.musiccatalog.dataaccesslayer.album.AlbumGenreEnum;
import com.musicstore.musiccatalog.dataaccesslayer.album.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumResponseModel extends RepresentationModel<AlbumResponseModel> {
    String albumId;
    String artistId; //from artist identifier
    String albumTitle;
    Integer releaseDate;
    String albumLength;
    AlbumGenreEnum albumGenre;
    Status status;
}
