package com.musicstore.musiccatalog.presentationlayer.artist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArtistResponseModel extends RepresentationModel<ArtistResponseModel> {
    String artistId;
    String artistName;
    String country;
    int debutYear;
    String biography;
}
