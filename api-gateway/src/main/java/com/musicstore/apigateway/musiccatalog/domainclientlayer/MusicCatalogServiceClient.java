package com.musicstore.apigateway.musiccatalog.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumRequestModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.AlbumResponseModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistRequestModel;
import com.musicstore.apigateway.musiccatalog.presentationlayer.ArtistResponseModel;
import com.musicstore.apigateway.utils.HttpErrorInfo;
import com.musicstore.apigateway.utils.exceptions.InvalidInputException;
import com.musicstore.apigateway.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class MusicCatalogServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String MUSIC_CATALOG_BASE_URL;

    public MusicCatalogServiceClient(RestTemplate restTemplate,
                                     ObjectMapper mapper,
                                     @Value("${app.musiccatalog-service.host}") String musicCatalogHost,
                                     @Value("${app.musiccatalog-service.port}") String musicCatalogPort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.MUSIC_CATALOG_BASE_URL = "http://" + musicCatalogHost + ":" + musicCatalogPort + "/api/v1";
    }

    // ==== ARTIST METHODS ====

    public List<ArtistResponseModel> getAllArtists() {
        String url = MUSIC_CATALOG_BASE_URL + "/artists";
        log.debug("MusicCatalogService GET all artists URL: " + url);
        try {
            ResponseEntity<List<ArtistResponseModel>> response =
                    restTemplate.exchange(url, HttpMethod.GET, null,
                            new ParameterizedTypeReference<>() {});
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public ArtistResponseModel getArtistByArtistId(String artistId) {
        String url = MUSIC_CATALOG_BASE_URL + "/artists/" + artistId;
        log.debug("MusicCatalogService GET artist by ID URL: " + url);
        try {
            return restTemplate.getForObject(url, ArtistResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public ArtistResponseModel addArtist(ArtistRequestModel request) {
        String url = MUSIC_CATALOG_BASE_URL + "/artists";
        log.debug("MusicCatalogService POST artist URL: " + url);
        try {
            return restTemplate.postForObject(url, request, ArtistResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public ArtistResponseModel updateArtist(ArtistRequestModel request, String artistId) {
        String url = MUSIC_CATALOG_BASE_URL + "/artists/" + artistId;
        log.debug("MusicCatalogService PUT artist URL: " + url);
        try {
            HttpEntity<ArtistRequestModel> entity = new HttpEntity<>(request);
            ResponseEntity<ArtistResponseModel> response =
                    restTemplate.exchange(url, HttpMethod.PUT, entity, ArtistResponseModel.class);
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteArtist(String artistId) {
        String url = MUSIC_CATALOG_BASE_URL + "/artists/" + artistId;
        log.debug("MusicCatalogService DELETE artist URL: " + url);
        try {
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    // ==== ALBUM METHODS ====

    public List<AlbumResponseModel> getAllAlbums(String artistId) {
        String url = MUSIC_CATALOG_BASE_URL + "/artists/" + artistId + "/albums";
        log.debug("MusicCatalogService GET all albums for artistId={} URL: {}", artistId, url);
        try {
            ResponseEntity<List<AlbumResponseModel>> response =
                    restTemplate.exchange(url, HttpMethod.GET, null,
                            new ParameterizedTypeReference<>() {});
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public AlbumResponseModel getAlbumByAlbumId(String artistId, String albumId) {
        String url = MUSIC_CATALOG_BASE_URL + "/artists/" + artistId + "/albums/" + albumId;
        log.debug("MusicCatalogService GET album by albumId URL: " + url);
        try {
            return restTemplate.getForObject(url, AlbumResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public AlbumResponseModel addAlbum(AlbumRequestModel request, String artistId) {
        String url = MUSIC_CATALOG_BASE_URL + "/artists/" + artistId + "/albums";
        log.debug("MusicCatalogService POST album for artistId={} URL: {}", artistId, url);
        try {
            return restTemplate.postForObject(url, request, AlbumResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public AlbumResponseModel updateAlbum(AlbumRequestModel request, String artistId, String albumId) {
        String url = MUSIC_CATALOG_BASE_URL + "/artists/" + artistId + "/albums/" + albumId;
        log.debug("MusicCatalogService PUT album URL: " + url);
        try {
            HttpEntity<AlbumRequestModel> entity = new HttpEntity<>(request);
            ResponseEntity<AlbumResponseModel> response =
                    restTemplate.exchange(url, HttpMethod.PUT, entity, AlbumResponseModel.class);
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteAlbum(String artistId, String albumId) {
        String url = MUSIC_CATALOG_BASE_URL + "/artists/" + artistId + "/albums/" + albumId;
        log.debug("MusicCatalogService DELETE album URL: " + url);
        try {
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    // ==== Error Handling Methods ====

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ioex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new NotFoundException(getErrorMessage(ex));
        } else if (ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(getErrorMessage(ex));
        }
        log.warn("Unexpected HTTP error: {}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ex;
    }
}

