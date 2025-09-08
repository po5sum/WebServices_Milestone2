package com.musicstore.musiccatalog.businesslayer.album;

import com.musicstore.musiccatalog.dataaccesslayer.album.*;
import com.musicstore.musiccatalog.dataaccesslayer.artist.Artist;
import com.musicstore.musiccatalog.dataaccesslayer.artist.ArtistRepository;
import com.musicstore.musiccatalog.mappinglayer.AlbumRequestMapper;
import com.musicstore.musiccatalog.mappinglayer.AlbumResponseMapper;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumRequestModel;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumResponseModel;
import com.musicstore.musiccatalog.utils.exceptions.InvalidInputException;
import com.musicstore.musiccatalog.utils.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlbumServiceImpl implements AlbumService {
    private final AlbumRepository albumRepository;
    private final AlbumRequestMapper albumRequestMapper;
    private final AlbumResponseMapper albumResponseMapper;
    private final ArtistRepository artistRepository;

    public AlbumServiceImpl(AlbumRepository albumRepository, AlbumRequestMapper albumRequestMapper, AlbumResponseMapper albumResponseMapper, ArtistRepository artistRepository) {
        this.albumRepository = albumRepository;
        this.albumRequestMapper = albumRequestMapper;
        this.albumResponseMapper = albumResponseMapper;
        this.artistRepository = artistRepository;
    }

    @Override
    public List<AlbumResponseModel> getAllAlbums(String artistId, Map<String, String> queryParams) {
        //looking for the album's artist
        Artist foundArtist = artistRepository.findByArtistIdentifier_ArtistId(artistId);
        if (foundArtist == null) {
            throw new NotFoundException("Artist not found");
        }

        //extract the query params
        String albumGenre = queryParams.get("albumGenre");

        //convert to enums
        Map<String, AlbumGenreEnum> albumGenreEnumMap = new HashMap<>();
        albumGenreEnumMap.put("rock", AlbumGenreEnum.ROCK);
        albumGenreEnumMap.put("pop", AlbumGenreEnum.POP);
        albumGenreEnumMap.put("jazz", AlbumGenreEnum.JAZZ);
        albumGenreEnumMap.put("classical", AlbumGenreEnum.CLASSICAL);
        albumGenreEnumMap.put("hip_hop", AlbumGenreEnum.HIP_HOP);
        albumGenreEnumMap.put("electronic", AlbumGenreEnum.ELECTRONIC);
        albumGenreEnumMap.put("indie_folk", AlbumGenreEnum.INDIE_FOLK);
        albumGenreEnumMap.put("experimental_pop", AlbumGenreEnum.EXPERIMENTAL_POP);
        albumGenreEnumMap.put("grunge", AlbumGenreEnum.GRUNGE);
        albumGenreEnumMap.put("reggae", AlbumGenreEnum.REGGAE);
        albumGenreEnumMap.put("alternative_rock", AlbumGenreEnum.ALTERNATIVE_ROCK);
        albumGenreEnumMap.put("pop_rock", AlbumGenreEnum.POP_ROCK);
        albumGenreEnumMap.put("glam_rock", AlbumGenreEnum.GLAM_ROCK);

        if(albumGenre != null) {
            return albumResponseMapper.entityListToResponseModelList
                    (albumRepository.findAlbumByArtistIdentifier_ArtistIdAndAlbumGenre(artistId, albumGenreEnumMap.get(albumGenre.toLowerCase())));
        }

        List<Album> albums = albumRepository.findAllByArtistIdentifier_ArtistId(artistId);
        return albumResponseMapper.entityListToResponseModelList(albums);
    }

    @Override
    public AlbumResponseModel getAlbumByAlbumId(String albumId) {
        Album album = albumRepository.findByAlbumIdentifier_AlbumId(albumId);
        if (album == null) {
            throw new NotFoundException("Provided album does not exist" + albumId);
        }
        return albumResponseMapper.entityToResponseModel(album);
    }

    @Override
    public AlbumResponseModel addAlbum(AlbumRequestModel albumRequestModel, String artistId) {
        Artist foundArtist = artistRepository.findByArtistIdentifier_ArtistId(artistId);
        if (foundArtist == null) {
            throw new NotFoundException("Artist not found");
        }

        if (albumRequestModel.getAlbumTitle() == null) {
            throw new InvalidInputException("Album title must not be null");
        }

        AlbumInformation albumInformation = new AlbumInformation(albumRequestModel.getAlbumTitle(), albumRequestModel.getReleaseDate(),
                albumRequestModel.getAlbumLength());

        Album album = albumRequestMapper.requestModelToEntity(albumRequestModel, new AlbumIdentifier(), albumInformation);
        album.setArtistIdentifier(foundArtist.getArtistIdentifier());
        album.setAlbumGenre(albumRequestModel.getAlbumGenre());
        return albumResponseMapper.entityToResponseModel(albumRepository.save(album));
    }

    @Override
    public AlbumResponseModel updateAlbum(AlbumRequestModel albumRequestModel, String artistId, String albumId) {
        Artist foundArtist = artistRepository.findByArtistIdentifier_ArtistId(artistId);
        if (foundArtist == null) {
            throw new NotFoundException("Provided artist does not exist" + artistId);
        }

        Album foundAlbum = albumRepository.findByAlbumIdentifier_AlbumId(albumId);
        if (foundAlbum == null) {
            throw new NotFoundException("Provided album does not exist" + albumId);
        }

        AlbumInformation albumInformation = new AlbumInformation(albumRequestModel.getAlbumTitle(), albumRequestModel.getReleaseDate(), albumRequestModel.getAlbumLength());
        Album toBeSaved = albumRequestMapper.requestModelToEntity(albumRequestModel, foundAlbum.getAlbumIdentifier(), albumInformation);
        toBeSaved.setId(foundAlbum.getId());
        toBeSaved.setArtistIdentifier(foundArtist.getArtistIdentifier());

        Album savedAlbum = albumRepository.save(toBeSaved);

        return albumResponseMapper.entityToResponseModel(savedAlbum);
    }

    @Override
    public void deleteAlbum(String artistId, String albumId) {
        Artist foundArtist = artistRepository.findByArtistIdentifier_ArtistId(artistId);
        if (foundArtist == null) {
            throw new NotFoundException("Provided artist does not exist" + artistId);
        }
        Album foundAlbum = albumRepository.findAlbumByArtistIdentifier_ArtistIdAndAlbumIdentifier_AlbumId(artistId, albumId);
        if (foundAlbum == null) {
            throw new NotFoundException("Provided album does not exist" + albumId);
        }
        albumRepository.delete(foundAlbum);
    }

    @Override
    public AlbumResponseModel updateCondition(String artistId, String albumId, Status newCondition) {
        Album foundAlbum = albumRepository.findAlbumByArtistIdentifier_ArtistIdAndAlbumIdentifier_AlbumId(artistId, albumId);
        if (foundAlbum == null) {
            throw new NotFoundException("Provided album does not exist" + albumId);
        }
        foundAlbum.setStatus(newCondition);
        Album savedAlbum = albumRepository.save(foundAlbum);
        return albumResponseMapper.entityToResponseModel(savedAlbum);
    }
}
