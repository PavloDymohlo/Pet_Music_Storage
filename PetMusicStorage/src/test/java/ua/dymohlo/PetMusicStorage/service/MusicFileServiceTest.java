package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.dymohlo.PetMusicStorage.dto.NewMusicFileDTO;
import ua.dymohlo.PetMusicStorage.dto.TransferMusicFilesToAnotherSubscription;
import ua.dymohlo.PetMusicStorage.dto.UpdateMusicFileNameDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateMusicFileSubscriptionDTO;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.repository.MusicFileRepository;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MusicFileServiceTest {

    @InjectMocks
    private MusicFileService musicFileService;
    @Mock
    private MusicFileRepository musicFileRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;


    @Test
    public void addMusicFile_success() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        NewMusicFileDTO newMusicFileDTO = NewMusicFileDTO.builder()
                .musicFileName("Music.mp3")
                .subscription(subscription).build();

        when(musicFileRepository.existsByMusicFileNameIgnoreCase("Music.mp3")).thenReturn(false);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);
        musicFileService.addMusicFile(newMusicFileDTO);
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("Music.mp3")
                .subscription(Subscription.builder()
                        .subscriptionName("FREE").build()).build();

        verify(musicFileRepository).save(musicFile);

    }

    @Test
    public void addMusicFile_musicFileAlreadyExists() {
        NewMusicFileDTO newMusicFileDTO = NewMusicFileDTO.builder()
                .musicFileName("MusicFile.mp3")
                .subscription(Subscription.builder()
                        .subscriptionName("FREE").build()).build();

        when(musicFileRepository.existsByMusicFileNameIgnoreCase("MusicFile.mp3")).thenReturn(true);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            musicFileService.addMusicFile(newMusicFileDTO);
        });

        assertEquals("Music file with musicName " + newMusicFileDTO.getMusicFileName() + " already exists", exception.getMessage());
    }

    @Test
    public void addMusicFile_subscriptionNotFound() {
        NewMusicFileDTO newMusicFileDTO = NewMusicFileDTO.builder()
                .musicFileName("MusicFile.mp3")
                .subscription(Subscription.builder()
                        .subscriptionName("FREE").build()).build();

        when(musicFileRepository.existsByMusicFileNameIgnoreCase("MusicFile.mp3")).thenReturn(false);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(null);
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.addMusicFile(newMusicFileDTO);
        });

        assertEquals("Subscription with subscriptionName " + newMusicFileDTO.getSubscription().getSubscriptionName() + " not found", exception.getMessage());
    }

    @Test
    public void findAllMusicFiles_success() {
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("Music.mp3").build();
        List<MusicFile> musicFiles = new ArrayList<>();
        musicFiles.add(musicFile);
        when(musicFileRepository.findAll()).thenReturn(musicFiles);

        musicFileService.findAllMusicFiles();

        assertFalse(musicFiles.isEmpty());
    }

    @Test
    public void findAllMusicFiles_listIsEmpty() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.findAllMusicFiles();
        });

        assertEquals("Any music files not found", exception.getMessage());
    }

    @Test
    public void findMusicFileById_success() {
        MusicFile musicFile = MusicFile.builder()
                .id(1L).build();
        when(musicFileRepository.findById(1L)).thenReturn(musicFile);

        MusicFile findMusicFile = musicFileService.findMusicFileById(1L);

        assertNotNull(findMusicFile);
    }

    @Test
    public void findMusicFileById_notFound() {
        long musicFileId = 2L;
        when(musicFileRepository.findById(2L)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.findMusicFileById(2L);
        });

        assertEquals("Music file with id " + musicFileId + " not found", exception.getMessage());
    }

    @Test
    public void findMusicFileByMusicFileName_success() {
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("Music.mp3").build();
        when(musicFileRepository.findByMusicFileNameIgnoreCase("Music.mp3")).thenReturn(musicFile);

        MusicFile findMusicFile = musicFileService.findMusicFileByMusicFileName("Music.mp3");

        assertNotNull(findMusicFile);
    }

    @Test
    public void findMusicFileByMusicFileName_notFound() {
        String musicFileName = "MusicFile.mp3";
        when(musicFileRepository.findByMusicFileNameIgnoreCase("MusicFile.mp3")).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.findMusicFileByMusicFileName("MusicFile.mp3");
        });

        assertEquals("Music file with musicFileName " + musicFileName + " not found", exception.getMessage());
    }

    @Test
    public void findMusicFilesBySubscription_success() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        MusicFile musicFile = MusicFile.builder()
                .subscription(subscription).build();
        List<MusicFile> musicFiles = new ArrayList<>();
        musicFiles.add(musicFile);
        subscription.setMusicFiles(musicFiles);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);

        List<MusicFile> findMusicFiles = musicFileService.findMusicFilesBySubscription("FREE");

        assertFalse(findMusicFiles.isEmpty());
    }

    @Test
    public void findMusicFilesBySubscription_subscriptionNotFound() {
        String subscriptionName = "PREMIUM";
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("PREMIUM")).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.findMusicFilesBySubscription("PREMIUM");
        });

        assertEquals("Subscription with name " + subscriptionName + " not found", exception.getMessage());
    }

    @Test
    public void findMusicFilesBySubscription_musicFilesNotFound() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("PREMIUM").build();
        List<MusicFile> musicFiles = new ArrayList<>();
        subscription.setMusicFiles(musicFiles);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("PREMIUM")).thenReturn(subscription);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.findMusicFilesBySubscription("PREMIUM");
        });

        assertEquals("Subscription with name " + subscription.getSubscriptionName() + " has no files", exception.getMessage());
    }

    @Test
    public void updateMusicFileName_success() {
        UpdateMusicFileNameDTO updateMusicFileNameDTO = UpdateMusicFileNameDTO.builder()
                .newMusicFileName("NewName.mp3")
                .currentMusicFileName("OldName.mp3").build();
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("OldName.mp3").build();
        when(musicFileRepository.findByMusicFileNameIgnoreCase("OldName.mp3")).thenReturn(musicFile);
        when(musicFileRepository.existsByMusicFileNameIgnoreCase("NewName.mp3")).thenReturn(false);

        musicFileService.updateMusicFileName(updateMusicFileNameDTO);
        musicFile = MusicFile.builder()
                .musicFileName("NewName.mp3").build();

        verify(musicFileRepository).save(musicFile);
    }

    @Test
    public void updateMusicFileName_notFound() {
        UpdateMusicFileNameDTO updateMusicFileNameDTO = UpdateMusicFileNameDTO.builder()
                .newMusicFileName("NewName.mp3")
                .currentMusicFileName("OldName.mp3").build();
        when(musicFileRepository.findByMusicFileNameIgnoreCase("OldName.mp3")).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.updateMusicFileName(updateMusicFileNameDTO);
        });

        assertEquals("Music file with name " + updateMusicFileNameDTO.getCurrentMusicFileName() + " not found", exception.getMessage());
    }

    @Test
    public void updateMusicFileName_musicFileAlreadyExists() {
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("OldName.mp3").build();
        UpdateMusicFileNameDTO updateMusicFileNameDTO = UpdateMusicFileNameDTO.builder()
                .newMusicFileName("NewName.mp3")
                .currentMusicFileName("OldName.mp3").build();
        when(musicFileRepository.findByMusicFileNameIgnoreCase("OldName.mp3")).thenReturn(musicFile);
        when(musicFileRepository.existsByMusicFileNameIgnoreCase("NewName.mp3")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            musicFileService.updateMusicFileName(updateMusicFileNameDTO);
        });

        assertEquals("This music name " + updateMusicFileNameDTO.getNewMusicFileName() + " already exists", exception.getMessage());
    }

    @Test
    public void updateMusicFileSubscription_success() {
        UpdateMusicFileSubscriptionDTO updateMusicFileSubscriptionDTO = UpdateMusicFileSubscriptionDTO.builder()
                .newMusicFileSubscription("PREMIUM")
                .musicFileName("OldName.mp3").build();
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("OldName.mp3").build();
        Subscription subscription = Subscription.builder()
                .subscriptionName("PREMIUM").build();
        when(musicFileRepository.findByMusicFileNameIgnoreCase("OldName.mp3")).thenReturn(musicFile);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("PREMIUM")).thenReturn(subscription);

        musicFileService.updateMusicFileSubscription(updateMusicFileSubscriptionDTO);
        musicFile = MusicFile.builder()
                .musicFileName("OldName.mp3")
                .subscription(subscription).build();

        verify(musicFileRepository).save(musicFile);
    }

    @Test
    public void updateMusicFileSubscription_subscriptionNotFound() {
        UpdateMusicFileSubscriptionDTO updateMusicFileSubscriptionDTO = UpdateMusicFileSubscriptionDTO.builder()
                .newMusicFileSubscription("PREMIUM")
                .musicFileName("OldName.mp3").build();
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("OldName.mp3").build();
        when(musicFileRepository.findByMusicFileNameIgnoreCase("OldName.mp3")).thenReturn(musicFile);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("PREMIUM")).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.updateMusicFileSubscription(updateMusicFileSubscriptionDTO);
        });

        assertEquals("Subscription with subscriptionName " + updateMusicFileSubscriptionDTO.getNewMusicFileSubscription() + " not found", exception.getMessage());
    }

    @Test
    public void transferMusicFilesToAnotherSubscription_success() {
        TransferMusicFilesToAnotherSubscription transferMusicFilesToAnotherSubscription = TransferMusicFilesToAnotherSubscription.builder()
                .currentMusicFilesSubscription("FREE")
                .newMusicFileSubscription("MAXIMUM").build();
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        Subscription newSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        List<MusicFile> musicFiles = new ArrayList<>();
        subscription.setMusicFiles(musicFiles);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("MAXIMUM")).thenReturn(newSubscription);

        musicFileService.transferMusicFilesToAnotherSubscription(transferMusicFilesToAnotherSubscription);
        List<MusicFile> newMusicFiles = subscription.getMusicFiles().stream()
                .peek(musicFile -> musicFile.setSubscription(newSubscription)).collect(Collectors.toList());

        verify(musicFileRepository).saveAll(newMusicFiles);
    }

    @Test
    public void transferMusicFilesToAnotherSubscription_subscriptionNotFound() {
        TransferMusicFilesToAnotherSubscription transferMusicFilesToAnotherSubscription = TransferMusicFilesToAnotherSubscription.builder()
                .currentMusicFilesSubscription("FREE")
                .newMusicFileSubscription("MAXIMUM").build();
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.transferMusicFilesToAnotherSubscription(transferMusicFilesToAnotherSubscription);
        });

        assertEquals("Subscription with subscriptionName " + transferMusicFilesToAnotherSubscription.getCurrentMusicFilesSubscription() + " not found", exception.getMessage());
    }

    @Test
    public void deleteAllMusicFiles_success() {
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("Music.mp3").build();
        List<MusicFile> musicFiles = new ArrayList<>();
        musicFiles.add(musicFile);
        when(musicFileRepository.findAll()).thenReturn(musicFiles);

        musicFileService.deleteAllMusicFiles();

        verify(musicFileRepository).deleteAll(musicFiles);
    }

    @Test
    public void deleteMusicFilesByMusicFileName_success() {
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("Music.mp3").build();
        when(musicFileRepository.findByMusicFileNameIgnoreCase(anyString())).thenReturn(musicFile);

        musicFileService.deleteMusicFilesByMusicFileName(anyString());

        verify(musicFileRepository).delete(musicFile);
    }

    @Test
    public void deleteMusicFilesByMusicFileName_notFound() {
        String musicFileName = "music.mp3";
        when(musicFileRepository.findByMusicFileNameIgnoreCase(anyString())).thenReturn(null);
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.deleteMusicFilesByMusicFileName(musicFileName);
        });

        assertEquals("Music file with name " + musicFileName + " not found", exception.getMessage());
    }

    @Test
    public void deleteMusicFilesById_success() {
        MusicFile musicFile = MusicFile.builder()
                .id(1L).build();
        when(musicFileRepository.findById(1L)).thenReturn(musicFile);

        musicFileService.deleteMusicFilesById(1L);

        verify(musicFileRepository).delete(musicFile);
    }

    @Test
    public void deleteMusicFilesById_notFound() {
        long musicFileId = 1L;
        when(musicFileRepository.findById(1L)).thenReturn(null);
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.deleteMusicFilesById(musicFileId);
        });

        assertEquals("Music file with id " + musicFileId + " not found", exception.getMessage());
    }

    @Test
    public void deleteMusicFilesBySubscription_success() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);
        List<MusicFile> musicFiles = subscription.getMusicFiles();

        musicFileService.deleteMusicFilesBySubscription("FREE");

        verify(musicFileRepository).deleteAll(musicFiles);
    }

    @Test
    public void deleteMusicFilesBySubscription_subscriptionNotFound() {
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(null);
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            musicFileService.deleteMusicFilesBySubscription("FREE");
        });

        assertEquals("Subscription with name FREE not found", exception.getMessage());
    }
}