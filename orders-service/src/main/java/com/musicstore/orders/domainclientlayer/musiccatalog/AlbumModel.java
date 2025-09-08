package com.musicstore.orders.domainclientlayer.musiccatalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class AlbumModel {
    String artistId;
    String artistName;
    String albumId;
    String albumTitle;
    Status status;
}
