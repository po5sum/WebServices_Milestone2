package com.musicstore.apigateway.musiccatalog.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArtistRequestModel {
    String artistName;
    String country;
    int debutYear;
    String biography;
}