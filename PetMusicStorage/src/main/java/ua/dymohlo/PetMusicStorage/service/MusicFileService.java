package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.dymohlo.PetMusicStorage.dto.NewMusicFileDTO;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.repository.MusicFileRepository;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicFileService {
    private final MusicFileRepository musicFileRepository;
    private final SubscriptionRepository subscriptionRepository;

    public void addMusicFile(NewMusicFileDTO newMusicFileDTO) {
        if (musicFileRepository.existsByMusicFileName(newMusicFileDTO.getMusicFileName())) {
            throw new IllegalArgumentException("Music file with musicName " + newMusicFileDTO.getMusicFileName() + " already exists");
        }
        Subscription subscription = subscriptionRepository.findBySubscriptionName(newMusicFileDTO.getSubscription().getSubscriptionName());
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + newMusicFileDTO.getSubscription().getSubscriptionName() + " not found");
        }
        MusicFile musicFile = MusicFile.builder()
                .musicFileName(newMusicFileDTO.getMusicFileName())
                .filePath(newMusicFileDTO.getFilePath())
                .subscription(subscription).build();
        musicFileRepository.save(musicFile);
    }

    public List<MusicFile> findAllMusicFiles() {
        List<MusicFile> musicFiles = musicFileRepository.findAll();
        if (musicFiles.isEmpty()) {
            throw new NoSuchElementException("Any music files not found");
        }
        return musicFiles;
    }

    public MusicFile findMusicFileById(long musicFileId) {
        MusicFile musicFile = musicFileRepository.findById(musicFileId);
        if (musicFile == null) {
            throw new NoSuchElementException("Music file with id " + musicFileId + " not found");
        }
        return musicFile;
    }

    public MusicFile findMusicFileByMusicFileName(String musicFileName) {
        MusicFile musicFile = musicFileRepository.findByMusicFileName(musicFileName);
        if (musicFile == null) {
            throw new NoSuchElementException("Music file with musicFileName " + musicFileName + " not found");
        }
        return musicFile;
    }

    public List<MusicFile> findMusicFilesBySubscription(String subscriptionName) {
        Subscription subscription = subscriptionRepository.findBySubscriptionName(subscriptionName);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with name " + subscriptionName + " not found");
        }
        List<MusicFile> musicFiles = subscription.getMusicFiles();
        if (musicFiles.isEmpty()) {
            throw new NoSuchElementException("Subscription with name " + subscriptionName + " has no files");
        }
        return musicFiles;
    }
}
