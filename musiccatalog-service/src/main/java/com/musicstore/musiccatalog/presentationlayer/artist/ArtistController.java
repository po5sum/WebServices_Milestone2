package com.musicstore.musiccatalog.presentationlayer.artist;

import com.musicstore.musiccatalog.businesslayer.artist.ArtistService;
import com.musicstore.musiccatalog.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/artists")
public class ArtistController {

    private final ArtistService artistService;
    private static final int UUID_LENGTH = 36;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping()
    public ResponseEntity<List<ArtistResponseModel>> getAllArtists() {
        return ResponseEntity.ok(artistService.getAllArtists());
    }

    @GetMapping("/{artistId}")
    public ResponseEntity<ArtistResponseModel> getArtistByArtistId(@PathVariable String artistId) {
        if (artistId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid artistId provided: " + artistId);
        }
        return ResponseEntity.ok().body(artistService.getArtistByArtistId(artistId));
    }

    @PostMapping()
    public ResponseEntity<ArtistResponseModel> addArtist(@RequestBody ArtistRequestModel artistRequestModel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(artistService.addArtist(artistRequestModel));
    }

    @PutMapping("/{artistId}")
    public ResponseEntity<ArtistResponseModel> updateArtist(@RequestBody ArtistRequestModel artistRequestModel, @PathVariable String artistId) {
        if (artistId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid artistId provided: " + artistId);
        }
        return ResponseEntity.ok().body(artistService.updateArtist(artistRequestModel, artistId));
    }

    @DeleteMapping("/{artistId}")
    public ResponseEntity<Void> deleteArtist(@PathVariable String artistId) {
        if (artistId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid artistId provided: " + artistId);
        }
        artistService.deleteArtist(artistId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
