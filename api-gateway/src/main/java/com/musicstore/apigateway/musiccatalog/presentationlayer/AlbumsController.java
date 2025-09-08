package com.musicstore.apigateway.musiccatalog.presentationlayer;

import com.musicstore.apigateway.musiccatalog.businesslayer.album.AlbumsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/artists/{artistId}/albums")
public class AlbumsController {
    private final AlbumsService albumsService;

    public AlbumsController(AlbumsService albumsService) {
        this.albumsService = albumsService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<AlbumResponseModel>> getAllAlbums(@PathVariable String artistId) {
        log.debug("Request received in AlbumsController: getAllAlbums for artistId={}", artistId);
        List<AlbumResponseModel> albums = albumsService.getAllAlbums(artistId);
        return ResponseEntity.ok().body(albums);
    }

    @GetMapping(value = "/{albumId}", produces = "application/json")
    public ResponseEntity<AlbumResponseModel> getAlbumByAlbumId(@PathVariable String artistId,
                                                           @PathVariable String albumId) {
        log.debug("Request received in AlbumsController: getAlbumById");
        AlbumResponseModel album = albumsService.getAlbumByAlbumId(artistId, albumId);
        return ResponseEntity.ok().body(album);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<AlbumResponseModel> addAlbum(@RequestBody AlbumRequestModel request,
                                                       @PathVariable String artistId) {
        log.debug("Request received in AlbumsController: addAlbum");
        AlbumResponseModel album = albumsService.addAlbum(request, artistId);
        return ResponseEntity.status(HttpStatus.CREATED).body(album);
    }

    @PutMapping(value = "/{albumId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AlbumResponseModel> updateAlbum(@RequestBody AlbumRequestModel request,
                                                          @PathVariable String artistId,
                                                          @PathVariable String albumId) {
        log.debug("Request received in AlbumsController: updateAlbum");
        AlbumResponseModel album = albumsService.updateAlbum(request, artistId, albumId);
        return ResponseEntity.ok().body(album);
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable String artistId,
                                            @PathVariable String albumId) {
        log.debug("Request received in AlbumsController: deleteAlbum");
        albumsService.deleteAlbum(artistId, albumId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping(@PathVariable String artistId) {
        return ResponseEntity.ok("AlbumsController is active for artistId=" + artistId);
    }

}