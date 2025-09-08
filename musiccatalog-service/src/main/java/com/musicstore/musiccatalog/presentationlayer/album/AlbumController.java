package com.musicstore.musiccatalog.presentationlayer.album;

import com.musicstore.musiccatalog.businesslayer.album.AlbumService;
import com.musicstore.musiccatalog.dataaccesslayer.album.Status;
import com.musicstore.musiccatalog.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/artists/{artistId}/albums")
public class AlbumController {
    private final AlbumService albumService;
    private static final int UUID_LENGTH = 36;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public ResponseEntity<List<AlbumResponseModel>> getAllAlbums(@PathVariable String artistId, @RequestParam(required = false) Map<String, String> queryParams ){
        if (artistId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid artistId provided: " + artistId);
        }
        return ResponseEntity.ok(albumService.getAllAlbums(artistId, queryParams));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<AlbumResponseModel> getAlbumByAlbumId(@PathVariable String albumId){
        if (albumId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid albumId provided: " + albumId);
        }
        return ResponseEntity.ok(albumService.getAlbumByAlbumId(albumId));
    }

    @PostMapping()
    public ResponseEntity<AlbumResponseModel> addAlbum(@RequestBody AlbumRequestModel albumRequestModel, @PathVariable String artistId){
        if (artistId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid artistId provided: " + artistId);
        }
        AlbumResponseModel created = albumService.addAlbum(albumRequestModel, artistId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<AlbumResponseModel> updateAlbum(@RequestBody AlbumRequestModel albumRequestModel, @PathVariable String artistId,
                                                          @PathVariable String albumId){
        if (artistId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid artistId provided: " + artistId);
        }
        if (albumId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid albumId provided: " + albumId);
        }
        return ResponseEntity.ok().body(albumService.updateAlbum(albumRequestModel, artistId, albumId));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable String artistId, @PathVariable String albumId){
        if (artistId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid artistId provided: " + artistId);
        }
        if (albumId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid albumId provided: " + albumId);
        }
        albumService.deleteAlbum(artistId, albumId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @PatchMapping("/{albumId}/condition")
    public ResponseEntity<AlbumResponseModel> patchCondition(
            @PathVariable String artistId,
            @PathVariable String albumId,
            @RequestBody String newConditionStr
    ) {
        Status newCondition = Status.valueOf(newConditionStr.toUpperCase());
        AlbumResponseModel updated = albumService.updateCondition(artistId, albumId, newCondition);
        return ResponseEntity.ok(updated);
    }
}
