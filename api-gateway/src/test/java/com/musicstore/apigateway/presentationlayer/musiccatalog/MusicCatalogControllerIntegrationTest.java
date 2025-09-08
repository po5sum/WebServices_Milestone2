package com.musicstore.apigateway.presentationlayer.musiccatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.apigateway.musiccatalog.domainclientlayer.MusicCatalogServiceClient;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumRequestModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumResponseModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistRequestModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class MusicCatalogControllerIntegrationTest {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MusicCatalogServiceClient musicClient;

    private MockRestServiceServer mockServer;
    private ObjectMapper mapper = new ObjectMapper();

    private static final String GATEWAY_ARTISTS = "/api/v1/artists";
    private static final String BACKEND_BASE = "http://localhost:7002/api/v1";

    private static final String VALID_ARTIST = "e5913a79-9b1e-4516-9ffd-06578e7af261";
    private static final String NOT_FOUND_ARTIST = VALID_ARTIST + "0";
    private static final String INVALID_ARTIST = "bad-uuid";

    private static final String GATEWAY_ALBUMS = "/api/v1/artists/{artistId}/albums";
    private static final String VALID_ALBUM = "84c5f33e-8e5d-4eb5-b35d-79272355fa72";
    private static final String NOT_FOUND_ALBUM = VALID_ALBUM + "0";
    private static final String INVALID_ALBUM = "not-uuid";

    @BeforeEach
    void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    // ----- ARTIST TESTS -----

    @Test
    void whenGetAllArtists_thenReturnList() throws Exception {
        List<ArtistResponseModel> list = List.of(
                ArtistResponseModel.builder().artistId(VALID_ARTIST).artistName("A").build(),
                ArtistResponseModel.builder().artistId(UUID.randomUUID().toString()).artistName("B").build()
        );

        mockServer.expect(ExpectedCount.once(), requestTo(BACKEND_BASE + "/artists"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(list), MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(GATEWAY_ARTISTS)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ArtistResponseModel.class)
                .hasSize(2)
                .value(l -> assertTrue(l.stream().anyMatch(a -> a.getArtistId().equals(VALID_ARTIST))));

        mockServer.verify();
    }

    @Test
    void whenGetArtistById_thenReturnArtist() throws Exception {
        ArtistResponseModel art = ArtistResponseModel.builder()
                .artistId(VALID_ARTIST).artistName("ArtistX").build();

        mockServer.expect(requestTo(BACKEND_BASE + "/artists/" + VALID_ARTIST))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(art), MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(GATEWAY_ARTISTS + "/" + VALID_ARTIST)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ArtistResponseModel.class)
                .value(a -> assertEquals("ArtistX", a.getArtistName()));
    }

     @Test
    void whenGetArtistInvalidId_thenReturn422() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(BACKEND_BASE + "/artists/" + INVALID_ARTIST))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid artistId provided: " + INVALID_ARTIST + "\"}"));

        webClient.get()
                .uri(GATEWAY_ARTISTS + "/" + INVALID_ARTIST)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
               .isEqualTo("Invalid artistId provided: " + INVALID_ARTIST);

       mockServer.verify();
    }
    @Test
    void whenAddArtistInvalidInput_thenReturn422() throws Exception {
        var req = ArtistRequestModel.builder().artistName("").build();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(BACKEND_BASE + "/artists"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Artist name must not be empty\"}"));

        webClient.post()
                .uri(GATEWAY_ARTISTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Artist name must not be empty");

        mockServer.verify();
    }

    // ----- NEW: HTTP error handling for Albums -----

    @Test
    void whenGetAllAlbumsInvalidArtist_thenReturn422() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(BACKEND_BASE + "/artists/" + INVALID_ARTIST + "/albums"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid artistId provided: " + INVALID_ARTIST + "\"}"));

        webClient.get()
                .uri(GATEWAY_ALBUMS, INVALID_ARTIST)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid artistId provided: " + INVALID_ARTIST);

        mockServer.verify();
    }

    @Test
    void whenGetAlbumByInvalidId_thenReturn422() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(BACKEND_BASE + "/artists/" + VALID_ARTIST + "/albums/" + INVALID_ALBUM))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid albumId provided: " + INVALID_ALBUM + "\"}"));

        webClient.get()
                .uri(GATEWAY_ALBUMS + "/" + INVALID_ALBUM, VALID_ARTIST)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid albumId provided: " + INVALID_ALBUM);

        mockServer.verify();
    }

    @Test
    void whenAddAlbumInvalidArtist_thenReturn422() throws Exception {
        var req = AlbumRequestModel.builder().albumTitle("X").build();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(BACKEND_BASE + "/artists/" + INVALID_ARTIST + "/albums"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid artistId provided: " + INVALID_ARTIST + "\"}"));

        webClient.post()
                .uri(GATEWAY_ALBUMS, INVALID_ARTIST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid artistId provided: " + INVALID_ARTIST);

        mockServer.verify();
    }

    @Test
    void whenArtistNotFound_then404() {
        mockServer.expect(requestTo(BACKEND_BASE + "/artists/" + NOT_FOUND_ARTIST))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("{\"message\":\"Not found\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        webClient.get().uri(GATEWAY_ARTISTS + "/" + NOT_FOUND_ARTIST)
                .exchange().expectStatus().isNotFound();
    }

    @Test
    void whenAddArtist_then201() throws Exception {
        ArtistRequestModel req = ArtistRequestModel.builder()
                .artistName("New").country("X").debutYear(2000).biography("B").build();
        ArtistResponseModel respModel = ArtistResponseModel.builder()
                .artistId(VALID_ARTIST).artistName("New").build();

        mockServer.expect(requestTo(BACKEND_BASE + "/artists"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(mapper.writeValueAsString(req)))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(respModel)));

        webClient.post().uri(GATEWAY_ARTISTS)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(req)
                .exchange().expectStatus().isCreated()
                .expectBody(ArtistResponseModel.class)
                .value(a -> assertEquals(VALID_ARTIST, a.getArtistId()));
    }

    @Test
    void whenUpdateArtist_then200() throws Exception {
        ArtistRequestModel upd = ArtistRequestModel.builder().artistName("Up").build();
        ArtistResponseModel respModel = ArtistResponseModel.builder()
                .artistId(VALID_ARTIST).artistName("Up").build();

        mockServer.expect(requestTo(BACKEND_BASE + "/artists/" + VALID_ARTIST))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(mapper.writeValueAsString(respModel), MediaType.APPLICATION_JSON));

        webClient.put().uri(GATEWAY_ARTISTS + "/" + VALID_ARTIST)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(upd)
                .exchange().expectStatus().isOk()
                .expectBody(ArtistResponseModel.class)
                .value(a -> assertEquals("Up", a.getArtistName()));
    }

    @Test
    void whenDeleteArtist_then204() {
        mockServer.expect(requestTo(BACKEND_BASE + "/artists/" + VALID_ARTIST))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        webClient.delete().uri(GATEWAY_ARTISTS + "/" + VALID_ARTIST)
                .exchange().expectStatus().isNoContent();
    }

    // ----- ALBUM TESTS -----

    @Test
    void whenGetAllAlbums_thenList() throws Exception {
        List<AlbumResponseModel> list = List.of(
                AlbumResponseModel.builder().albumId(VALID_ALBUM).build()
        );
        mockServer.expect(requestTo(BACKEND_BASE + "/artists/" + VALID_ARTIST + "/albums"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(list), MediaType.APPLICATION_JSON));

        webClient.get().uri(GATEWAY_ALBUMS, VALID_ARTIST)
                .exchange().expectStatus().isOk()
                .expectBodyList(AlbumResponseModel.class)
                .hasSize(1);
    }

    @Test
    void whenGetAlbumById_then200() throws Exception {
        AlbumResponseModel a = AlbumResponseModel.builder().albumId(VALID_ALBUM).build();
        mockServer.expect(requestTo(BACKEND_BASE + "/artists/" + VALID_ARTIST + "/albums/" + VALID_ALBUM))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(a), MediaType.APPLICATION_JSON));

        webClient.get().uri(GATEWAY_ALBUMS + "/" + VALID_ALBUM, VALID_ARTIST)
                .exchange().expectStatus().isOk()
                .expectBody(AlbumResponseModel.class)
                .value(r -> assertEquals(VALID_ALBUM, r.getAlbumId()));
    }

    @Test
    void whenAddAlbum_then201() throws Exception {
        AlbumRequestModel req = AlbumRequestModel.builder()
                .albumTitle("X").releaseDate(2025).build();
        AlbumResponseModel resp = AlbumResponseModel.builder()
                .albumId(VALID_ALBUM).artistId(VALID_ARTIST).albumTitle("X").releaseDate(2025).build();

        mockServer.expect(requestTo(BACKEND_BASE + "/artists/" + VALID_ARTIST + "/albums"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(mapper.writeValueAsString(req))) // Ensure request body matches
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(resp))); // Ensure response body matches

        webClient.post().uri(GATEWAY_ALBUMS, VALID_ARTIST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AlbumResponseModel.class)
                .value(r -> assertEquals(VALID_ALBUM, r.getAlbumId()));
    }
    @Test
    void whenUpdateAlbum_then200() throws Exception {
        AlbumResponseModel resp = AlbumResponseModel.builder()
                .albumId(VALID_ALBUM).build();
        mockServer.expect(requestTo(BACKEND_BASE + "/artists/" + VALID_ARTIST + "/albums/" + VALID_ALBUM))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(mapper.writeValueAsString(resp), MediaType.APPLICATION_JSON));

        webClient.put().uri(GATEWAY_ALBUMS + "/" + VALID_ALBUM, VALID_ARTIST)
                .bodyValue(AlbumRequestModel.builder().build())
                .exchange().expectStatus().isOk();
    }

    @Test
    void whenDeleteAlbum_then204() {
        mockServer.expect(requestTo(BACKEND_BASE + "/artists/" + VALID_ARTIST + "/albums/" + VALID_ALBUM))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        webClient.delete().uri(GATEWAY_ALBUMS + "/" + VALID_ALBUM, VALID_ARTIST)
                .exchange().expectStatus().isNoContent();
    }
}

