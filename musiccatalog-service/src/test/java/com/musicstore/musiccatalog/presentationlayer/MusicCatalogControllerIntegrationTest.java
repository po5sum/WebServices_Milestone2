package com.musicstore.musiccatalog.presentationlayer;

import com.musicstore.musiccatalog.dataaccesslayer.album.AlbumGenreEnum;
import com.musicstore.musiccatalog.dataaccesslayer.album.AlbumRepository;
import com.musicstore.musiccatalog.dataaccesslayer.artist.ArtistRepository;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumRequestModel;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumResponseModel;
import com.musicstore.musiccatalog.presentationlayer.artist.ArtistRequestModel;
import com.musicstore.musiccatalog.presentationlayer.artist.ArtistResponseModel;
import com.musicstore.musiccatalog.utils.exceptions.DuplicateArtistNameException;
import com.musicstore.musiccatalog.utils.exceptions.InvalidInputException;
import com.musicstore.musiccatalog.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data-h2.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MusicCatalogControllerIntegrationTest {
    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private WebTestClient webTestClient;

    private final String BASE_URL_ARTISTS = "/api/v1/artists";
    private final String VALID_ARTIST_ID = "e5913a79-9b1e-4516-9ffd-06578e7af261";
    private final String NOT_FOUND_ARTIST_ID = "e5913a79-9b1e-4516-9ffd-06578e7af262";
    private final String INVALID_ARTIST_ID = "c3540a89-cb47-4c96-888e-ff96708d";

    private final String BASE_URL_ALBUM = "/api/v1/artists/{artistId}/albums";
    private final String VALID_ALBUM_ID = "84c5f33e-8e5d-4eb5-b35d-79272355fa72";
    private final String NOT_FOUND_ALBUM_ID = "84c5f33e-8e5d-4eb5-b35d-79272355fa73";
    private final String INVALID_ALBUM_ID = "c3540a89-cb47-4c96-888e-ff96708d";

    @Test
    public void whenGetAllArtists_thenReturnList() {
        List<ArtistResponseModel> list = webTestClient.get()
                .uri(BASE_URL_ARTISTS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ArtistResponseModel.class)
                .returnResult().getResponseBody();

        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertEquals(artistRepository.count(), list.size());
    }

    @Test
    public void whenGetArtistByValidId_thenReturnArtist() {
        webTestClient.get()
                .uri(BASE_URL_ARTISTS + "/" + VALID_ARTIST_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ArtistResponseModel.class)
                .value(artist -> assertEquals(VALID_ARTIST_ID, artist.getArtistId()));
    }

    @Test
    public void whenGetArtistByInvalidId_thenReturnUnprocessableEntity() {
        webTestClient.get()
                .uri(BASE_URL_ARTISTS + "/" + INVALID_ARTIST_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid artistId provided: " + INVALID_ARTIST_ID);
    }

    @Test
    public void whenGetArtistByNotFoundId_thenReturnNotFound() {
        webTestClient.get()
                .uri(BASE_URL_ARTISTS + "/" + NOT_FOUND_ARTIST_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Provided artist does not exist" + NOT_FOUND_ARTIST_ID);
    }

    @Test
    public void whenAddArtistValid_thenReturnCreated() {
        ArtistRequestModel req = ArtistRequestModel.builder()
                .artistName("Queen")
                .country("UK")
                .debutYear(1970)
                .biography("British rock band")
                .build();

        webTestClient.post()
                .uri(BASE_URL_ARTISTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ArtistResponseModel.class)
                .value(artist -> {
                    assertNotNull(artist.getArtistId());
                    assertEquals("Queen", artist.getArtistName());
                });
    }

    @Test
    public void whenAddArtistDuplicateName_thenReturnUnprocessableEntity() {
        // "The Beatles" is already in data‑h2.sql
        ArtistRequestModel req = ArtistRequestModel.builder()
                .artistName("The Beatles")
                .country("UK")
                .debutYear(1960)
                .biography("Legendary band")
                .build();

        webTestClient.post()
                .uri(BASE_URL_ARTISTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .value(msg -> assertTrue(((String)msg).contains("already exists")));
    }

    @Test
    public void whenUpdateArtistValid_thenReturnOk() {
        ArtistRequestModel update = ArtistRequestModel.builder()
                .artistName("U2")
                .country("Ireland")
                .debutYear(1976)
                .biography("Irish rock band")
                .build();

        webTestClient.put()
                .uri(BASE_URL_ARTISTS + "/" + VALID_ARTIST_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ArtistResponseModel.class)
                .value(artist -> assertEquals("U2", artist.getArtistName()));
    }

    @Test
    public void whenUpdateArtistWithInvalidId_thenReturnUnprocessableEntity() {
        ArtistRequestModel update = ArtistRequestModel.builder()
                .artistName("NewName")
                .country("X")
                .debutYear(2000)
                .biography("Bio")
                .build();

        webTestClient.put()
                .uri(BASE_URL_ARTISTS + "/" + INVALID_ARTIST_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid artistId provided: " + INVALID_ARTIST_ID);
    }

    @Test
    public void whenUpdateArtistNotFound_thenReturnNotFound() {
        ArtistRequestModel update = ArtistRequestModel.builder()
                .artistName("NoOne")
                .country("X")
                .debutYear(2000)
                .biography("Bio")
                .build();

        webTestClient.put()
                .uri(BASE_URL_ARTISTS + "/" + NOT_FOUND_ARTIST_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Provided artist does not exist" + NOT_FOUND_ARTIST_ID);
    }

    @Test
    public void whenDeleteArtistValid_thenReturnNoContent() {
        webTestClient.delete()
                .uri(BASE_URL_ARTISTS + "/" + VALID_ARTIST_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void whenDeleteArtistWithInvalidId_thenReturnUnprocessableEntity() {
        webTestClient.delete()
                .uri(BASE_URL_ARTISTS + "/" + INVALID_ARTIST_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid artistId provided: " + INVALID_ARTIST_ID);
    }

    @Test
    public void whenDeleteArtistNotFound_thenReturnNotFound() {
        webTestClient.delete()
                .uri(BASE_URL_ARTISTS + "/" + NOT_FOUND_ARTIST_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Provided artist does not exist" + NOT_FOUND_ARTIST_ID);
    }

    // -------------------- ALBUM TESTS --------------------

    @Test
    public void whenGetAllAlbumsForValidArtist_thenReturnList() {
        long sizeDb = albumRepository.findAllByArtistIdentifier_ArtistId(VALID_ARTIST_ID).size();

        webTestClient.get()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AlbumResponseModel.class)
                .value(list -> assertEquals(sizeDb, list.size()));
    }

    @Test
    public void whenGetAllAlbumsForInvalidArtist_thenReturnUnprocessableEntity() {
        webTestClient.get()
                .uri(BASE_URL_ALBUM.replace("{artistId}", INVALID_ARTIST_ID))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid artistId provided: " + INVALID_ARTIST_ID);
    }

    @Test
    public void whenGetAllAlbumsForNotFoundArtist_thenReturnNotFound() {
        webTestClient.get()
                .uri(BASE_URL_ALBUM.replace("{artistId}", NOT_FOUND_ARTIST_ID))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Artist not found");
    }

    @Test
    public void whenGetAlbumByValidId_thenReturnAlbum() {
        webTestClient.get()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID) + "/" + VALID_ALBUM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AlbumResponseModel.class)
                .value(album -> assertEquals(VALID_ALBUM_ID, album.getAlbumId()));
    }

    @Test
    public void whenGetAlbumByInvalidId_thenReturnUnprocessableEntity() {
        webTestClient.get()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID) + "/" + INVALID_ALBUM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid albumId provided: " + INVALID_ALBUM_ID);
    }

    @Test
    public void whenGetAlbumByNotFoundId_thenReturnNotFound() {
        webTestClient.get()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID) + "/" + NOT_FOUND_ALBUM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Provided album does not exist" + NOT_FOUND_ALBUM_ID);
    }

    @Test
    public void whenAddAlbumValid_thenReturnCreated() {
        AlbumRequestModel req = AlbumRequestModel.builder()
                .artistId(VALID_ARTIST_ID)
                .albumTitle("New Album")
                .releaseDate(2025)
                .albumLength("00:42")
                .albumGenre(AlbumGenreEnum.ROCK)
                .build();

        webTestClient.post()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AlbumResponseModel.class)
                .value(album -> {
                    assertNotNull(album.getAlbumId());
                    assertEquals("New Album", album.getAlbumTitle());
                });
    }

    @Test
    public void whenAddAlbumInvalidArtist_thenReturnUnprocessableEntity() {
        AlbumRequestModel req = AlbumRequestModel.builder()
                .artistId(INVALID_ARTIST_ID)
                .albumTitle("X")
                .releaseDate(2000)
                .albumLength("00:10")
                .albumGenre(AlbumGenreEnum.POP)
                .build();

        webTestClient.post()
                .uri(BASE_URL_ALBUM.replace("{artistId}", INVALID_ARTIST_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid artistId provided: " + INVALID_ARTIST_ID);
    }

    @Test
    public void whenAddAlbumNotFoundArtist_thenReturnNotFound() {
        AlbumRequestModel req = AlbumRequestModel.builder()
                .artistId(NOT_FOUND_ARTIST_ID)
                .albumTitle("X")
                .releaseDate(2000)
                .albumLength("00:10")
                .albumGenre(AlbumGenreEnum.JAZZ)
                .build();

        webTestClient.post()
                .uri(BASE_URL_ALBUM.replace("{artistId}", NOT_FOUND_ARTIST_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Artist not found");
    }

    @Test
    public void whenUpdateAlbumValid_thenReturnOk() {
        AlbumRequestModel update = AlbumRequestModel.builder()
                .artistId(VALID_ARTIST_ID)
                .albumTitle("Updated Title")
                .releaseDate(2022)
                .albumLength("00:50")
                .albumGenre(AlbumGenreEnum.POP)
                .build();

        webTestClient.put()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID) + "/" + VALID_ALBUM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AlbumResponseModel.class)
                .value(album -> assertEquals("Updated Title", album.getAlbumTitle()));
    }

    @Test
    public void whenUpdateAlbumInvalidIds_thenReturnUnprocessableEntity() {
        AlbumRequestModel update = AlbumRequestModel.builder()
                .artistId(VALID_ARTIST_ID)
                .albumTitle("X")
                .releaseDate(2000)
                .albumLength("00:10")
                .albumGenre(AlbumGenreEnum.CLASSICAL)
                .build();

        // invalid artist
        webTestClient.put()
                .uri(BASE_URL_ALBUM.replace("{artistId}", INVALID_ARTIST_ID) + "/" + VALID_ALBUM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        // invalid album
        webTestClient.put()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID) + "/" + INVALID_ALBUM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenUpdateAlbumNotFound_thenReturnNotFound() {
        AlbumRequestModel update = AlbumRequestModel.builder()
                .artistId(VALID_ARTIST_ID)
                .albumTitle("Y")
                .releaseDate(2001)
                .albumLength("00:11")
                .albumGenre(AlbumGenreEnum.REGGAE)
                .build();

        // artist exists but album not found
        webTestClient.put()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID) + "/" + NOT_FOUND_ALBUM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Provided album does not exist" + NOT_FOUND_ALBUM_ID);
    }

    @Test
    public void whenDeleteAlbumValid_thenReturnNoContent() {
        webTestClient.delete()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID) + "/" + VALID_ALBUM_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void whenDeleteAlbumInvalidIds_thenReturnUnprocessableEntity() {
        // invalid artist
        webTestClient.delete()
                .uri(BASE_URL_ALBUM.replace("{artistId}", INVALID_ARTIST_ID) + "/" + VALID_ALBUM_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        // invalid album
        webTestClient.delete()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID) + "/" + INVALID_ALBUM_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenDeleteAlbumNotFound_thenReturnNotFound() {
        webTestClient.delete()
                .uri(BASE_URL_ALBUM.replace("{artistId}", VALID_ARTIST_ID) + "/" + NOT_FOUND_ALBUM_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Provided album does not exist" + NOT_FOUND_ALBUM_ID);
    }

    @Test
    public void testInvalidInputExceptionConstructors() {
        // no-arg
        InvalidInputException ex1 = new InvalidInputException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        // message-only
        String msg = "Invalid input";
        InvalidInputException ex2 = new InvalidInputException(msg);
        assertEquals(msg, ex2.getMessage());

        // cause-only
        RuntimeException cause = new RuntimeException("cause");
        InvalidInputException ex3 = new InvalidInputException(cause);
        assertSame(cause, ex3.getCause());

        // message + cause
        String msg2 = "Invalid input with cause";
        RuntimeException cause2 = new RuntimeException("cause2");
        InvalidInputException ex4 = new InvalidInputException(msg2, cause2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(cause2, ex4.getCause());
    }

    @Test
    public void testNotFoundExceptionConstructors() {
        // no-arg
        NotFoundException ex1 = new NotFoundException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        // message-only
        String msg = "Not found";
        NotFoundException ex2 = new NotFoundException(msg);
        assertEquals(msg, ex2.getMessage());

        // cause-only
        RuntimeException cause = new RuntimeException("cause");
        NotFoundException ex3 = new NotFoundException(cause);
        assertSame(cause, ex3.getCause());

        // message + cause
        String msg2 = "Not found with cause";
        RuntimeException cause2 = new RuntimeException("cause2");
        NotFoundException ex4 = new NotFoundException(msg2, cause2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(cause2, ex4.getCause());
    }

    @Test
    public void DuplicateArtistNameException() {
        // no-arg
        DuplicateArtistNameException ex1 = new DuplicateArtistNameException();
        assertNull(ex1.getMessage());
        assertNull(ex1.getCause());

        // message-only
        String msg = "dup email";
        DuplicateArtistNameException ex2 = new DuplicateArtistNameException(msg);
        assertEquals(msg, ex2.getMessage());

        // cause-only
        RuntimeException cause = new RuntimeException("boom");
        DuplicateArtistNameException ex3 = new DuplicateArtistNameException(cause);
        assertSame(cause, ex3.getCause());

        // message + cause
        String msg2 = "dup2";
        RuntimeException cause2 = new RuntimeException("kaboom");
        DuplicateArtistNameException ex4 = new DuplicateArtistNameException(msg2, cause2);
        assertEquals(msg2, ex4.getMessage());
        assertSame(cause2, ex4.getCause());
    }
}
