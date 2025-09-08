package com.musicstore.musiccatalog.dataaccesslayer;

import com.musicstore.musiccatalog.businesslayer.album.AlbumService;
import com.musicstore.musiccatalog.businesslayer.album.AlbumServiceImpl;
import com.musicstore.musiccatalog.dataaccesslayer.album.*;
import com.musicstore.musiccatalog.dataaccesslayer.artist.Artist;
import com.musicstore.musiccatalog.dataaccesslayer.artist.ArtistIdentifier;
import com.musicstore.musiccatalog.dataaccesslayer.artist.ArtistInformation;
import com.musicstore.musiccatalog.dataaccesslayer.artist.ArtistRepository;
import com.musicstore.musiccatalog.mappinglayer.AlbumRequestMapper;
import com.musicstore.musiccatalog.mappinglayer.AlbumResponseMapper;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumRequestModel;
import com.musicstore.musiccatalog.presentationlayer.album.AlbumResponseModel;
import com.musicstore.musiccatalog.utils.exceptions.InvalidInputException;
import com.musicstore.musiccatalog.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MusicCatalogRepositoryIntegrationTest {
    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;


    @Test
    public void whenArtistExists_thenReturnAllArtists() {
        // arrange
        Artist a1 = new Artist();
        a1.setArtistIdentifier(new ArtistIdentifier("id-1"));
        a1.setArtistInformation(new ArtistInformation("Name1", "C1", 2001, "Bio1"));
        Artist a2 = new Artist();
        a2.setArtistIdentifier(new ArtistIdentifier("id-2"));
        a2.setArtistInformation(new ArtistInformation("Name2", "C2", 2002, "Bio2"));
        artistRepository.save(a1);
        artistRepository.save(a2);
        long count = artistRepository.count();

        // act
        List<Artist> list = artistRepository.findAll();

        // assert
        assertNotNull(list);
        assertNotEquals(0, count);
        assertEquals(count, list.size());
    }

    @Test
    public void whenFindByArtistId_thenReturnArtist() {
        // arrange
        Artist toSave = new Artist();
        toSave.setArtistIdentifier(new ArtistIdentifier("test-artist-1"));
        toSave.setArtistInformation(
                new ArtistInformation("Name", "Country", 2000, "Bio")
        );
        artistRepository.save(toSave);

        // act
        Artist found = artistRepository.findByArtistIdentifier_ArtistId("test-artist-1");

        // assert
        assertNotNull(found);
        assertEquals("test-artist-1", found.getArtistIdentifier().getArtistId());
    }

    @Test
    public void whenFindByUnknownArtistId_thenReturnNull() {
        Artist found = artistRepository.findByArtistIdentifier_ArtistId("no-such-id");
        assertNull(found);
    }

    @Test
    public void whenArtistEntityIsValid_thenAddArtist() {
        Artist toSave = new Artist();
        toSave.setArtistIdentifier(new ArtistIdentifier("test-artist-2"));
        toSave.setArtistInformation(
                new ArtistInformation("Another", "X", 1999, "Bio2")
        );

        Artist saved = artistRepository.save(toSave);
        assertNotNull(saved);
        assertNotNull(saved.getArtistIdentifier());
        assertEquals("Another",
                saved.getArtistInformation().getArtistName());
    }

    /*
    @Test
    public void whenSavingArtistWithNullName_thenThrowException() {
        Artist invalid = new Artist();
        invalid.setArtistIdentifier(new ArtistIdentifier("bad-id"));
        // Missing artistName, which should violate @NotNull
        invalid.setArtistInformation(new ArtistInformation(null, "Country", 2000, "Bio"));

        assertThrows(DataIntegrityViolationException.class, () -> {
            artistRepository.saveAndFlush(invalid);
        });
    }

     */


    @Test
    public void whenArtistUpdated_thenChangesArePersisted() {
        Artist artist = new Artist();
        artist.setArtistIdentifier(new ArtistIdentifier("update-artist"));
        artist.setArtistInformation(new ArtistInformation("NameX", "CA", 1990, "Old Bio"));

        Artist saved = artistRepository.save(artist);

        // Create new ArtistInformation and assign it
        ArtistInformation updatedInfo = new ArtistInformation("NameX", "CA", 1990, "New Bio");
        saved.setArtistInformation(updatedInfo);
        artistRepository.save(saved);

        Artist updated = artistRepository.findByArtistIdentifier_ArtistId("update-artist");
        assertEquals("New Bio", updated.getArtistInformation().getBiography());
    }

    @Test
    public void whenUpdateNonExistentArtist_thenCreateNewRecord() {
        Artist ghost = new Artist();
        ghost.setArtistIdentifier(new ArtistIdentifier("ghost-id"));
        ghost.setArtistInformation(
                new ArtistInformation("Ghost Artist", "Nowhere", 1900, "Phantom bio")
        );

        long countBefore = artistRepository.count();

        Artist saved = artistRepository.save(ghost);

        assertNotNull(saved);
        assertNotNull(saved.getId()); // should be assigned by JPA
        assertEquals(countBefore + 1, artistRepository.count()); // confirms it was inserted
    }



    @Test
    public void whenArtistEntityIsDeleted_thenReturnNull() {
        Artist orig = new Artist();
        orig.setArtistIdentifier(new ArtistIdentifier("test-artist-4"));
        orig.setArtistInformation(
                new ArtistInformation("Bar", "Q", 1980, "C")
        );
        artistRepository.save(orig);

        Artist toDelete = artistRepository.findByArtistIdentifier_ArtistId("test-artist-4");
        artistRepository.delete(toDelete);

        Artist found = artistRepository.findByArtistIdentifier_ArtistId("test-artist-4");
        assertNull(found);
    }

    @Test
    public void whenDeleteNonExistentArtist_thenNoExceptionThrown() {
        Artist ghost = new Artist();
        ghost.setArtistIdentifier(new ArtistIdentifier("ghost-id"));
        ghost.setArtistInformation(new ArtistInformation("Ghost", "Nowhere", 1900, "Not real"));

        assertDoesNotThrow(() -> artistRepository.delete(ghost));
    }


    @Test
    public void whenExistsByArtistName_thenReturnTrue() {
        Artist a = new Artist();
        a.setArtistIdentifier(new ArtistIdentifier("test-artist-5"));
        a.setArtistInformation(
                new ArtistInformation("UniqueName", "W", 2010, "Bio")
        );
        artistRepository.save(a);

        boolean exists = artistRepository.existsByArtistInformation_ArtistName("UniqueName");
        assertTrue(exists);
    }

    @Test
    public void whenDoesNotExistByArtistName_thenReturnFalse() {
        boolean exists = artistRepository.existsByArtistInformation_ArtistName("NoSuchName");
        assertFalse(exists);
    }

    @Test
    public void whenFindAllArtists_thenReturnEmptyListIfNoArtistsExist() {
        artistRepository.deleteAll();  // already empty but for clarity
        List<Artist> all = artistRepository.findAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    public void testArtistIdentifierConstructorAndGetter() {
        String uuid = "id-123";
        ArtistIdentifier id = new ArtistIdentifier(uuid);
        assertEquals(uuid, id.getArtistId());
    }

    @Test
    public void whenArtistConstructorIsCalled_thenAllFieldsAreInitialized() {
        ArtistInformation info = new ArtistInformation("NameX", "CX", 2020, "B1");
        ArtistIdentifier id = new ArtistIdentifier("id-456");

        Artist a = new Artist();
        a.setArtistIdentifier(id);
        a.setArtistInformation(info);

        assertNotNull(a.getArtistIdentifier());
        assertEquals("NameX", a.getArtistInformation().getArtistName());
    }

    // ------ ALBUM REPOSITORY ------

    @Test
    public void whenAlbumExistsForArtist_thenReturnAllAlbums() {
        Album alb1 = new Album();
        alb1.setAlbumIdentifier(new AlbumIdentifier("alb-1"));
        alb1.setArtistIdentifier(new ArtistIdentifier("artist-1"));
        alb1.setAlbumGenre(AlbumGenreEnum.ROCK);
        alb1.setAlbumInformation(new AlbumInformation("T", 2022, "3:00"));
        albumRepository.save(alb1);

        List<Album> list = albumRepository.findAllByArtistIdentifier_ArtistId("artist-1");
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    public void whenFindByAlbumId_thenReturnAlbum() {
        Album alb = new Album();
        alb.setAlbumIdentifier(new AlbumIdentifier("alb-2"));
        alb.setArtistIdentifier(new ArtistIdentifier("artist-2"));
        alb.setAlbumGenre(AlbumGenreEnum.POP);
        alb.setAlbumInformation(new AlbumInformation("U", 2021, "4:00"));
        albumRepository.save(alb);

        Album found = albumRepository.findByAlbumIdentifier_AlbumId("alb-2");
        assertNotNull(found);
        assertEquals("alb-2", found.getAlbumIdentifier().getAlbumId());
    }

    @Test
    public void whenFindByUnknownAlbumId_thenReturnNull() {
        Album found = albumRepository.findByAlbumIdentifier_AlbumId("no-alb");
        assertNull(found);
    }

    @Test
    public void whenFindAlbumByArtistAndAlbum_thenReturnAlbum() {
        Album alb = new Album();
        alb.setAlbumIdentifier(new AlbumIdentifier("alb-3"));
        alb.setArtistIdentifier(new ArtistIdentifier("artist-3"));
        alb.setAlbumGenre(AlbumGenreEnum.JAZZ);
        alb.setAlbumInformation(new AlbumInformation("O", 2020, "5:00"));
        albumRepository.save(alb);

        Album found = albumRepository
                .findAlbumByArtistIdentifier_ArtistIdAndAlbumIdentifier_AlbumId(
                        "artist-3", "alb-3");
        assertNotNull(found);
        assertEquals(AlbumGenreEnum.JAZZ, found.getAlbumGenre());
    }

    @Test
    public void whenFindByGenre_thenReturnMatchingAlbums() {
        Album alb = new Album();
        alb.setAlbumIdentifier(new AlbumIdentifier("alb-4"));
        alb.setArtistIdentifier(new ArtistIdentifier("artist-4"));
        alb.setAlbumGenre(AlbumGenreEnum.CLASSICAL);
        alb.setAlbumInformation(new AlbumInformation("C", 2019, "6:00"));
        albumRepository.save(alb);

        List<Album> list = albumRepository
                .findAlbumByArtistIdentifier_ArtistIdAndAlbumGenre(
                        "artist-4", AlbumGenreEnum.CLASSICAL);
        assertNotNull(list);
        assertEquals(1, list.size());
    }
    @Test
    public void whenValidAlbumSaved_thenAlbumIsPersisted() {
        Album album = new Album();
        album.setAlbumIdentifier(new AlbumIdentifier("valid-save-alb-1"));
        album.setArtistIdentifier(new ArtistIdentifier("artist-save-1"));
        album.setAlbumGenre(AlbumGenreEnum.ELECTRONIC);
        album.setAlbumInformation(new AlbumInformation("Electric Feel", 2012, "4:20"));

        Album saved = albumRepository.save(album);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Electric Feel", saved.getAlbumInformation().getAlbumTitle());
        assertEquals(AlbumGenreEnum.ELECTRONIC, saved.getAlbumGenre());
    }

    /*
    @Test
    public void whenAlbumTitleIsNull_thenThrowInvalidInputException() {
        AlbumRequestModel req = AlbumRequestModel.builder()
                .albumTitle(null)  // required field
                .releaseDate(2022)
                .albumLength("3:30")
                .albumGenre(AlbumGenreEnum.ROCK)
                .build();

        String validArtistId = "some-valid-artist-id"; // Make sure this artist exists in your test setup or mock

        assertThrows(InvalidInputException.class, () -> {
            albumService.addAlbum(req, validArtistId);
        });
    }
     */


    @Test
    public void whenAlbumUpdated_thenReturnUpdatedAlbum() {
        Album alb = new Album();
        alb.setAlbumIdentifier(new AlbumIdentifier("alb-upd-1"));
        alb.setArtistIdentifier(new ArtistIdentifier("artist-upd"));
        alb.setAlbumGenre(AlbumGenreEnum.INDIE_FOLK);
        alb.setAlbumInformation(new AlbumInformation("Initial", 2017, "3:30"));

        Album saved = albumRepository.save(alb);

        // Update fields
        saved.setAlbumInformation(new AlbumInformation("UpdatedTitle", 2018, "4:00"));
        Album updated = albumRepository.save(saved);

        assertEquals("UpdatedTitle", updated.getAlbumInformation().getAlbumTitle());
    }

    @Test
    public void whenUpdateNonExistentAlbum_thenInsertNewAlbum() {
        Album alb = new Album();
        alb.setAlbumIdentifier(new AlbumIdentifier("ghost-alb-1"));
        alb.setArtistIdentifier(new ArtistIdentifier("ghost-artist-1"));
        alb.setAlbumGenre(AlbumGenreEnum.EXPERIMENTAL_POP);
        alb.setAlbumInformation(new AlbumInformation("Ghost Album", 2016, "2:22"));

        long before = albumRepository.count();

        Album inserted = albumRepository.save(alb);

        assertNotNull(inserted);
        assertNotNull(inserted.getId());
        assertEquals(before + 1, albumRepository.count());
    }

    @Test
    public void whenDeleteAlbum_thenReturnNullOnFind() {
        Album alb = new Album();
        alb.setAlbumIdentifier(new AlbumIdentifier("alb-5"));
        alb.setArtistIdentifier(new ArtistIdentifier("artist-5"));
        alb.setAlbumGenre(AlbumGenreEnum.POP_ROCK);
        alb.setAlbumInformation(new AlbumInformation("P", 2018, "7:00"));
        albumRepository.save(alb);

        Album toDelete = albumRepository.findByAlbumIdentifier_AlbumId("alb-5");
        albumRepository.delete(toDelete);

        Album found = albumRepository.findByAlbumIdentifier_AlbumId("alb-5");
        assertNull(found);
    }
    @Test
    public void whenDeleteNonExistentAlbum_thenNoExceptionThrown() {
        Album ghost = new Album();
        ghost.setAlbumIdentifier(new AlbumIdentifier("ghost-alb-2"));
        ghost.setArtistIdentifier(new ArtistIdentifier("ghost-artist-2"));
        ghost.setAlbumGenre(AlbumGenreEnum.GRUNGE);
        ghost.setAlbumInformation(new AlbumInformation("Missing", 2015, "3:33"));

        assertDoesNotThrow(() -> albumRepository.delete(ghost));
    }

    private AlbumServiceImpl albumService() {
        // pull in both mappers
        var reqMapper  = Mappers.getMapper(AlbumRequestMapper.class);
        var respMapper = Mappers.getMapper(AlbumResponseMapper.class);
        return new AlbumServiceImpl(albumRepository, reqMapper, respMapper, artistRepository);
    }

    @Test
    public void whenUpdateConditionValid_thenStatusIsPersisted() {
        // arrange
        String artistId = "artist-update-test";
        String albumId  = "album-update-test";
        Album alb = new Album();
        alb.setArtistIdentifier(new ArtistIdentifier(artistId));
        alb.setAlbumIdentifier(new AlbumIdentifier(albumId));
        alb.setAlbumGenre(AlbumGenreEnum.ROCK);
        alb.setAlbumInformation(new AlbumInformation("Title", 2020, "3:00"));
        albumRepository.save(alb);

        // act
        AlbumResponseModel updated = albumService()
                .updateCondition(artistId, albumId, Status.USED);

        // assert DTO and DB both updated
        assertEquals(Status.USED, updated.getStatus());
        Album fromDb = albumRepository
                .findAlbumByArtistIdentifier_ArtistIdAndAlbumIdentifier_AlbumId(artistId, albumId);
        assertEquals(Status.USED, fromDb.getStatus());
    }

    @Test
    public void whenUpdateConditionNonexistent_thenThrowNotFound() {
        // act & assert
        assertThrows(
                NotFoundException.class,
                () -> albumService().updateCondition("no-art", "no-alb", Status.NEW)
        );
    }
}