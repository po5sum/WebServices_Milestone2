package com.musicstore.orders.domainclientlayer.musiccatalog;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.orders.utils.HttpErrorInfo;
import com.musicstore.orders.utils.exceptions.InvalidInputException;
import com.musicstore.orders.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

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
    public AlbumModel getArtistByArtistId(String artistId) {
        String url = MUSIC_CATALOG_BASE_URL + "/artists/" + artistId;
        log.debug("MusicCatalogService GET artist by ID URL: " + url);
        try {
            return restTemplate.getForObject(url, AlbumModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    // ==== ALBUM METHODS ====
    public AlbumModel getAlbumByAlbumId(String artistId, String albumId) {
        try {
            String url = MUSIC_CATALOG_BASE_URL + "/artists/" + artistId + "/albums/" + albumId;
            log.debug("MusicCatalogService GET album by albumId URL: " + url);

            String response = restTemplate.getForObject(url, String.class);

            return ACLAlbumModelFromJsonString(response);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public AlbumModel patchAlbumConditionTypeByArtistAndAlbumId(
            String artistId,
            String albumId,
            Status newCondition) {

        String url = MUSIC_CATALOG_BASE_URL
                + "/artists/" + artistId
                + "/albums/"  + albumId
                + "/condition";                      // e.g. PATCH /artists/{artistId}/albums/{albumId}/condition

        log.debug("MusicCatalogService PATCH album condition URL: {}", url);
        try {
            // send just the enum name as the new payload
            String response = restTemplate
                    .patchForObject(url, newCondition.toString(), String.class);

            // parse that JSON back into our ACL AlbumModel
            return ACLAlbumModelFromJsonString(response);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing patched album JSON", e);
        }
    }

    private AlbumModel ACLAlbumModelFromJsonString(String response) throws JsonProcessingException {
        JsonNode node = mapper.readTree(response);

        String artistId    = node.path("artistId").asText();
        String albumId     = node.path("albumId").asText();
        String artistName  = node.path("artistName").asText();
        String albumTitle  = node.path("albumTitle").asText();

        // parse conditionType, default to NEW if missing or invalid
        String cond = node.path("conditionType").asText();
        Status condition;
        try {
            condition = cond.isEmpty()
                    ? Status.NEW
                    : Status.valueOf(cond.toUpperCase());
        } catch (IllegalArgumentException e) {
            condition = Status.NEW;
        }

        return AlbumModel.builder()
                .artistId(   artistId)
                .albumId(    albumId)
                .artistName( artistName)
                .albumTitle( albumTitle)
                .status(condition)
                .build();
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

