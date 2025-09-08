package com.musicstore.musiccatalog.presentationlayer.artist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistRequestModel {
    String artistName;
    String country;
    int debutYear;
    String biography;
}
