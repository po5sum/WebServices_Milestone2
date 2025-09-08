package com.musicstore.apigateway.presentationlayer.musiccatalog;

import com.musicstore.apigateway.musiccatalog.businesslayer.album.AlbumsService;
import com.musicstore.apigateway.musiccatalog.businesslayer.artist.ArtistsService;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumRequestModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumResponseModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumsController;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistRequestModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistResponseModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistsController;
import com.musicstore.apigateway.utils.exceptions.InvalidInputException;
import com.musicstore.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class MusicCatalogControllerUnitTest {
    @Autowired
    private ArtistsController artistsController;

    @Autowired
    private AlbumsController albumsController;

    @MockitoBean
    private ArtistsService artistsService;

    @MockitoBean
    private AlbumsService albumsService;

    private static final String VALID_ARTIST = "e5913a79-9b1e-4516-9ffd-06578e7af261";
    private static final String NOT_FOUND_ARTIST = VALID_ARTIST + "0";
    private static final String INVALID_ARTIST = "bad-uuid";

    private static final String VALID_ALBUM = "84c5f33e-8e5d-4eb5-b35d-79272355fa72";
    private static final String NOT_FOUND_ALBUM = VALID_ALBUM + "0";
    private static final String INVALID_ALBUM = "not-uuid";

    // ArtistsController unit tests

    @Test
    void whenGetAllArtists_thenReturnList() {
        ArtistResponseModel a1 = ArtistResponseModel.builder()
                .artistId(VALID_ARTIST)
                .artistName("A")
                .country(null)
                .debutYear(0)
                .biography(null)
                .build();
        ArtistResponseModel a2 = ArtistResponseModel.builder()
                .artistId("id2")
                .artistName("B")
                .country(null)
                .debutYear(0)
                .biography(null)
                .build();
        when(artistsService.getAllArtists()).thenReturn(List.of(a1, a2));

        ResponseEntity<List<ArtistResponseModel>> resp = artistsController.getAllArtists();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().size());
        verify(artistsService, times(1)).getAllArtists();
    }

    @Test
    void whenGetArtistByValidId_thenReturnArtist() {
        ArtistResponseModel art = ArtistResponseModel.builder()
                .artistId(VALID_ARTIST)
                .artistName("ArtistX")
                .country(null)
                .debutYear(0)
                .biography(null)
                .build();
        when(artistsService.getArtistByArtistId(VALID_ARTIST)).thenReturn(art);

        ResponseEntity<ArtistResponseModel> resp = artistsController.getArtistById(VALID_ARTIST);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(art, resp.getBody());
        verify(artistsService).getArtistByArtistId(VALID_ARTIST);
    }

    @Test
    void whenGetArtistInvalidId_thenThrowInvalidInput() {
        when(artistsService.getArtistByArtistId(INVALID_ARTIST))
                .thenThrow(new InvalidInputException("Invalid id"));

        assertThrows(InvalidInputException.class,
                () -> artistsController.getArtistById(INVALID_ARTIST));
    }

    @Test
    void whenGetArtistNotFound_thenThrowNotFound() {
        when(artistsService.getArtistByArtistId(NOT_FOUND_ARTIST))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class,
                () -> artistsController.getArtistById(NOT_FOUND_ARTIST));
    }

    @Test
    void whenAddArtistValid_thenReturnCreated() {
        ArtistRequestModel req = new ArtistRequestModel("Queen", "UK", 1970, "Bio");
        ArtistResponseModel respModel = ArtistResponseModel.builder()
                .artistId("newId")
                .artistName("Queen")
                .country(null)
                .debutYear(1970)
                .biography(null)
                .build();
        when(artistsService.addArtist(req)).thenReturn(respModel);

        ResponseEntity<ArtistResponseModel> resp = artistsController.addArtist(req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertSame(respModel, resp.getBody());
        verify(artistsService).addArtist(req);
    }

    @Test
    void whenAddArtistDuplicate_thenThrowInvalidInput() {
        var req = new ArtistRequestModel("Beatles", "UK", 1960, "Bio");
        when(artistsService.addArtist(req))
                .thenThrow(new InvalidInputException("already exists"));

        assertThrows(InvalidInputException.class,
                () -> artistsController.addArtist(req));
    }

    @Test
    void whenUpdateArtistValid_thenReturnOk() {
        ArtistRequestModel req = new ArtistRequestModel("U2", "Ireland", 1976, "Bio");
        ArtistResponseModel updated = ArtistResponseModel.builder()
                .artistId(VALID_ARTIST)
                .artistName("U2")
                .country(null)
                .debutYear(1976)
                .biography(null)
                .build();
        when(artistsService.updateArtist(req, VALID_ARTIST)).thenReturn(updated);

        ResponseEntity<ArtistResponseModel> resp = artistsController.updateArtist(req, VALID_ARTIST);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(updated, resp.getBody());
        verify(artistsService).updateArtist(req, VALID_ARTIST);
    }

    @Test
    void whenUpdateArtistInvalid_thenThrowInvalidInput() {
        var req = new ArtistRequestModel("X", "Y", 2000, "Bio");
        when(artistsService.updateArtist(req, INVALID_ARTIST))
                .thenThrow(new InvalidInputException("Invalid id"));

        assertThrows(InvalidInputException.class,
                () -> artistsController.updateArtist(req, INVALID_ARTIST));
    }

    @Test
    void whenUpdateArtistNotFound_thenThrowNotFound() {
        var req = new ArtistRequestModel("NoOne", "X", 2000, "Bio");
        when(artistsService.updateArtist(req, NOT_FOUND_ARTIST))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class,
                () -> artistsController.updateArtist(req, NOT_FOUND_ARTIST));
    }

    @Test
    void whenDeleteArtistValid_thenReturnNoContent() {
        doNothing().when(artistsService).deleteArtist(VALID_ARTIST);

        ResponseEntity<Void> resp =
                artistsController.deleteArtist(VALID_ARTIST);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(artistsService).deleteArtist(VALID_ARTIST);
    }

    @Test
    void whenDeleteArtistInvalid_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid id"))
                .when(artistsService).deleteArtist(INVALID_ARTIST);

        assertThrows(InvalidInputException.class,
                () -> artistsController.deleteArtist(INVALID_ARTIST));
    }

    @Test
    void whenDeleteArtistNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException("Not found"))
                .when(artistsService).deleteArtist(NOT_FOUND_ARTIST);

        assertThrows(NotFoundException.class,
                () -> artistsController.deleteArtist(NOT_FOUND_ARTIST));
    }

    // AlbumsController unit tests

    @Test
    void whenGetAllAlbumsValid_thenReturnList() {
        var al = new AlbumResponseModel("al1", VALID_ARTIST, "T", 2020, "00:10", null, null);
        when(albumsService.getAllAlbums(VALID_ARTIST)).thenReturn(List.of(al));

        ResponseEntity<List<AlbumResponseModel>> resp =
                albumsController.getAllAlbums(VALID_ARTIST);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
        verify(albumsService).getAllAlbums(VALID_ARTIST);
    }

    @Test
    void whenGetAllAlbumsInvalid_thenThrowInvalidInput() {
        when(albumsService.getAllAlbums(INVALID_ARTIST))
                .thenThrow(new InvalidInputException("Invalid"));

        assertThrows(InvalidInputException.class,
                () -> albumsController.getAllAlbums(INVALID_ARTIST));
    }

    @Test
    void whenGetAllAlbumsNotFound_thenThrowNotFound() {
        when(albumsService.getAllAlbums(NOT_FOUND_ARTIST))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class,
                () -> albumsController.getAllAlbums(NOT_FOUND_ARTIST));
    }

    @Test
    void whenGetAlbumByValid_thenReturnAlbum() {
        var al = new AlbumResponseModel(VALID_ALBUM, VALID_ARTIST, "T", 2020, "00:10", null, null);
        when(albumsService.getAlbumByAlbumId(VALID_ARTIST, VALID_ALBUM)).thenReturn(al);

        ResponseEntity<AlbumResponseModel> resp =
                albumsController.getAlbumByAlbumId(VALID_ARTIST, VALID_ALBUM);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(al, resp.getBody());
        verify(albumsService).getAlbumByAlbumId(VALID_ARTIST, VALID_ALBUM);
    }

    @Test
    void whenGetAlbumByInvalid_thenThrowInvalidInput() {
        when(albumsService.getAlbumByAlbumId(VALID_ARTIST, INVALID_ALBUM))
                .thenThrow(new InvalidInputException("Invalid"));

        assertThrows(InvalidInputException.class,
                () -> albumsController.getAlbumByAlbumId(VALID_ARTIST, INVALID_ALBUM));
    }

    @Test
    void whenGetAlbumNotFound_thenThrowNotFound() {
        when(albumsService.getAlbumByAlbumId(VALID_ARTIST, NOT_FOUND_ALBUM))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class,
                () -> albumsController.getAlbumByAlbumId(VALID_ARTIST, NOT_FOUND_ALBUM));
    }

    @Test
    void whenAddAlbumValid_thenReturnCreated() {
        var req = new AlbumRequestModel(null, "Title", 2025, "00:10", null, null);
        var created = new AlbumResponseModel("alNew", VALID_ARTIST, "Title", 2025, "00:10", null, null);
        when(albumsService.addAlbum(req, VALID_ARTIST)).thenReturn(created);

        ResponseEntity<AlbumResponseModel> resp =
                albumsController.addAlbum(req, VALID_ARTIST);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertSame(created, resp.getBody());
        verify(albumsService).addAlbum(req, VALID_ARTIST);
    }

    @Test
    void whenAddAlbumInvalidArtist_thenThrowInvalidInput() {
        var req = new AlbumRequestModel(null, "X", 0, null, null, null);
        when(albumsService.addAlbum(req, INVALID_ARTIST))
                .thenThrow(new InvalidInputException("Invalid"));

        assertThrows(InvalidInputException.class,
                () -> albumsController.addAlbum(req, INVALID_ARTIST));
    }

    @Test
    void whenAddAlbumNotFoundArtist_thenThrowNotFound() {
        var req = new AlbumRequestModel(null, "X", 0, null, null, null);
        when(albumsService.addAlbum(req, NOT_FOUND_ARTIST))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class,
                () -> albumsController.addAlbum(req, NOT_FOUND_ARTIST));
    }

    @Test
    void whenUpdateAlbumValid_thenReturnOk() {
        var req = new AlbumRequestModel(null, "Up", 2021, null, null, null);
        var updated = new AlbumResponseModel(VALID_ALBUM, VALID_ARTIST, "Up", 2021, null, null, null);
        when(albumsService.updateAlbum(req, VALID_ARTIST, VALID_ALBUM)).thenReturn(updated);

        ResponseEntity<AlbumResponseModel> resp =
                albumsController.updateAlbum(req, VALID_ARTIST, VALID_ALBUM);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(updated, resp.getBody());
        verify(albumsService).updateAlbum(req, VALID_ARTIST, VALID_ALBUM);
    }

    @Test
    void whenUpdateAlbumInvalid_thenThrowInvalidInput() {
        var req = new AlbumRequestModel(null, "X", 0, null, null, null);
        when(albumsService.updateAlbum(req, VALID_ARTIST, INVALID_ALBUM))
                .thenThrow(new InvalidInputException("Invalid"));

        assertThrows(InvalidInputException.class,
                () -> albumsController.updateAlbum(req, VALID_ARTIST, INVALID_ALBUM));
    }

    @Test
    void whenUpdateAlbumNotFound_thenThrowNotFound() {
        var req = new AlbumRequestModel(null, "X", 0, null, null, null);
        when(albumsService.updateAlbum(req, VALID_ARTIST, NOT_FOUND_ALBUM))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class,
                () -> albumsController.updateAlbum(req, VALID_ARTIST, NOT_FOUND_ALBUM));
    }

    @Test
    void whenDeleteAlbumValid_thenReturnNoContent() {
        doNothing().when(albumsService).deleteAlbum(VALID_ARTIST, VALID_ALBUM);

        ResponseEntity<Void> resp =
                albumsController.deleteAlbum(VALID_ARTIST, VALID_ALBUM);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(albumsService).deleteAlbum(VALID_ARTIST, VALID_ALBUM);
    }

    @Test
    void whenDeleteAlbumInvalid_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid"))
                .when(albumsService).deleteAlbum(INVALID_ARTIST, VALID_ALBUM);

        assertThrows(InvalidInputException.class,
                () -> albumsController.deleteAlbum(INVALID_ARTIST, VALID_ALBUM));
    }

    @Test
    void whenDeleteAlbumNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException("Not found"))
                .when(albumsService).deleteAlbum(VALID_ARTIST, NOT_FOUND_ALBUM);

        assertThrows(NotFoundException.class,
                () -> albumsController.deleteAlbum(VALID_ARTIST, NOT_FOUND_ALBUM));
    }

    @Test
    void whenPing_thenReturnOkMessage() {
        ResponseEntity<String> resp = albumsController.ping(VALID_ARTIST);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().contains("AlbumsController is active for artistId=" + VALID_ARTIST));
    }
}

