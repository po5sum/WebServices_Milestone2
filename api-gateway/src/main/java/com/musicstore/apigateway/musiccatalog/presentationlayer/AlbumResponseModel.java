package com.musicstore.apigateway.musiccatalog.presentationlayer;

import com.musicstore.apigateway.musiccatalog.domainclientlayer.AlbumGenreEnum;
import com.musicstore.apigateway.musiccatalog.domainclientlayer.Status;
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
