package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
public class MusicFileRepositoryTest {
    @Mock
    private MusicFileRepository musicFileRepository;
    @BeforeEach
    void setUp(){
        MusicFile musicFile = MusicFile.builder()
                .id(1L)
                .musicFileName("Music.mp3").build();
        List<MusicFile> musicFiles = new ArrayList<>();
        musicFiles.add(musicFile);
        when(musicFileRepository.existsByMusicFileNameIgnoreCase("Music.mp3")).thenReturn(true);
        when(musicFileRepository.findById(1L)).thenReturn(musicFile);
        when(musicFileRepository.findByMusicFileNameIgnoreCase("Music.mp3")).thenReturn(musicFile);
    }

    @Test
    public void existsByMusicFileName_exists(){
        String musicName = "Music.mp3";
        boolean existMusicFile = musicFileRepository.existsByMusicFileNameIgnoreCase(musicName);

        assertTrue(existMusicFile);
    }

    @Test
    public void existsByMusicFileName_notFound(){
        String musicName = "Song.mp3";
        boolean existMusicFile = musicFileRepository.existsByMusicFileNameIgnoreCase(musicName);

        assertFalse(existMusicFile);
    }

    @Test
    public void findAllMusicFiles_returnNull(){
        List<MusicFile> foundMusicList = musicFileRepository.findAll();
        assertTrue(foundMusicList.isEmpty());
    }

    @Test
    public void findMusicFileById_exists(){
        long musicFileId = 1l;
        MusicFile existMusicFile = musicFileRepository.findById(musicFileId);

        assertNotNull(existMusicFile);
    }

    @Test
    public void findMusicFileById_notFound(){
        long musicFileId = 2l;
        MusicFile existMusicFile = musicFileRepository.findById(musicFileId);

        assertNull(existMusicFile);
    }

    @Test
    public void findByMusicFileName_exists(){
        String musicName = "Music.mp3";
        MusicFile foundMusicFile = musicFileRepository.findByMusicFileNameIgnoreCase(musicName);

        assertNotNull(foundMusicFile);
    }

    @Test
    public void findByMusicFileName_notFound(){
        String musicName = "Song.mp3";
        MusicFile foundMusicFile = musicFileRepository.findByMusicFileNameIgnoreCase(musicName);

        assertNull(foundMusicFile);
    }
}
