package com.musicstore.musiccatalog.businesslayer.artist;

import com.musicstore.musiccatalog.dataaccesslayer.artist.Artist;
import com.musicstore.musiccatalog.dataaccesslayer.artist.ArtistIdentifier;
import com.musicstore.musiccatalog.dataaccesslayer.artist.ArtistInformation;
import com.musicstore.musiccatalog.dataaccesslayer.artist.ArtistRepository;
import com.musicstore.musiccatalog.mappinglayer.ArtistRequestMapper;
import com.musicstore.musiccatalog.mappinglayer.ArtistResponseMapper;
import com.musicstore.musiccatalog.presentationlayer.artist.ArtistRequestModel;
import com.musicstore.musiccatalog.presentationlayer.artist.ArtistResponseModel;
import com.musicstore.musiccatalog.utils.exceptions.NotFoundException;
import com.musicstore.musiccatalog.utils.exceptions.DuplicateArtistNameException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ArtistServiceImpl implements ArtistService {
    private final ArtistRepository artistRepository;
    private final ArtistResponseMapper artistResponseMapper;
    private final ArtistRequestMapper artistRequestMapper;

    public ArtistServiceImpl(ArtistRepository artistRepository, ArtistResponseMapper artistResponseMapper, ArtistRequestMapper artistRequestMapper) {
        this.artistRepository = artistRepository;
        this.artistResponseMapper = artistResponseMapper;
        this.artistRequestMapper = artistRequestMapper;
    }


    @Override
    public List<ArtistResponseModel> getAllArtists() {
        List<Artist> artists = artistRepository.findAll();
        return artistResponseMapper.entityListToResponseModelList(artists);
    }

    @Override
    public ArtistResponseModel getArtistByArtistId(String artistId) {
        Artist artist = artistRepository.findByArtistIdentifier_ArtistId(artistId);

        if (artist == null) {
            throw new NotFoundException("Provided artist does not exist" + artistId);
        }
        return artistResponseMapper.entityToResponseModel(artist);
    }

    @Override
    public ArtistResponseModel addArtist(ArtistRequestModel artistRequestModel) {
        if (artistRepository.existsByArtistInformation_ArtistName(artistRequestModel.getArtistName())) {
            throw new DuplicateArtistNameException("Artist with name '" + artistRequestModel.getArtistName() + "' already exists.");
        }

        ArtistInformation information = new ArtistInformation(artistRequestModel.getArtistName(), artistRequestModel.getCountry(),
                artistRequestModel.getDebutYear(), artistRequestModel.getBiography() );

        Artist artist = artistRequestMapper.requestModelToEntity(artistRequestModel, new ArtistIdentifier(), information);
        artist.setArtistInformation(information);
        return artistResponseMapper.entityToResponseModel(artistRepository.save(artist));
    }

    @Override
    public ArtistResponseModel updateArtist(ArtistRequestModel artistRequestModel, String artistId) {
        if (artistRepository.existsByArtistInformation_ArtistName(artistRequestModel.getArtistName())) {
            throw new DuplicateArtistNameException("Artist with name '" + artistRequestModel.getArtistName() + "' already exists.");
        }

        Artist existingArtist = artistRepository.findByArtistIdentifier_ArtistId(artistId);

        if(existingArtist == null) {
            throw new NotFoundException("Provided artist does not exist" + artistId);
        }
        ArtistInformation information = new ArtistInformation(artistRequestModel.getArtistName(), artistRequestModel.getCountry(),
                artistRequestModel.getDebutYear(), artistRequestModel.getBiography() );

        Artist updatedArtist = artistRequestMapper.requestModelToEntity(artistRequestModel, existingArtist.getArtistIdentifier(), information);
        updatedArtist.setId(existingArtist.getId());

        Artist response = artistRepository.save(updatedArtist);
        return artistResponseMapper.entityToResponseModel(response);
    }

    @Override
    public void deleteArtist(String artistId) {
        Artist existingArtist = artistRepository.findByArtistIdentifier_ArtistId(artistId);

        if(existingArtist == null){
            throw new NotFoundException("Provided artist does not exist" + artistId);
        }
        artistRepository.delete(existingArtist);
    }
}
