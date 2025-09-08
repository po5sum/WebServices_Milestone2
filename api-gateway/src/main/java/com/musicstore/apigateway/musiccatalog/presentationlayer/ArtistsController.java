package com.musicstore.apigateway.musiccatalog.presentationlayer;


import com.musicstore.apigateway.musiccatalog.businesslayer.artist.ArtistsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/artists")
public class ArtistsController {
    private final ArtistsService artistsService;

    public ArtistsController(ArtistsService artistsService) {
        this.artistsService = artistsService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<ArtistResponseModel>> getAllArtists() {
        log.debug("Request received in ArtistsController: getAllArtists");
        List<ArtistResponseModel> artists = artistsService.getAllArtists();
        return ResponseEntity.ok().body(artists);
    }

    @GetMapping(value = "/{artistId}", produces = "application/json")
    public ResponseEntity<ArtistResponseModel> getArtistById(@PathVariable("artistId") String artistId) {
        log.debug("Request received in ArtistsController: getArtistById");
        ArtistResponseModel artist = artistsService.getArtistByArtistId(artistId);
        return ResponseEntity.ok().body(artist);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ArtistResponseModel> addArtist(@RequestBody ArtistRequestModel request) {
        log.debug("Request received in ArtistsController: addArtist");
        ArtistResponseModel artist = artistsService.addArtist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(artist);
    }

    @PutMapping(value = "/{artistId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ArtistResponseModel> updateArtist(
            @RequestBody ArtistRequestModel request,
            @PathVariable("artistId") String artistId) {
        log.debug("Request received in ArtistsController: updateArtist");
        ArtistResponseModel artist = artistsService.updateArtist(request, artistId);
        return ResponseEntity.ok().body(artist);
    }

    @DeleteMapping("/{artistId}")
    public ResponseEntity<Void> deleteArtist(@PathVariable("artistId") String artistId) {
        log.debug("Request received in ArtistsController: deleteArtist");
        artistsService.deleteArtist(artistId);
        return ResponseEntity.noContent().build();
    }
}

