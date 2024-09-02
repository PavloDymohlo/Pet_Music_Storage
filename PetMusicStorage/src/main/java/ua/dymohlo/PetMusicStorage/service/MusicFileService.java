package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicFileService {
    private final MusicFileRepository musicFileRepository;
    private final SubscriptionRepository subscriptionRepository;

    public void addMusicFile(NewMusicFileDTO newMusicFileDTO) {
        if (musicFileRepository.existsByMusicFileNameIgnoreCase(newMusicFileDTO.getMusicFileName())) {
            throw new IllegalArgumentException("Music file with musicName " + newMusicFileDTO.getMusicFileName() + " already exists");
        }
        Subscription subscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(newMusicFileDTO.getSubscription().getSubscriptionName());
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
        MusicFile musicFile = musicFileRepository.findByMusicFileNameIgnoreCase(musicFileName);
        if (musicFile == null) {
            throw new NoSuchElementException("Music file with musicFileName " + musicFileName + " not found");
        }
        return musicFile;
    }

    public List<MusicFile> findMusicFilesBySubscription(String subscriptionName) {
        Subscription subscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(subscriptionName);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with name " + subscriptionName + " not found");
        }
        List<MusicFile> musicFiles = subscription.getMusicFiles();
        if (musicFiles.isEmpty()) {
            throw new NoSuchElementException("Subscription with name " + subscriptionName + " has no files");
        }
        return musicFiles;
    }

    public void updateMusicFileName(UpdateMusicFileNameDTO updateMusicFileNameDTO) {
        MusicFile musicFile = musicFileRepository.findByMusicFileNameIgnoreCase(updateMusicFileNameDTO.getCurrentMusicFileName());
        boolean musicFileNewName = musicFileRepository.existsByMusicFileNameIgnoreCase(updateMusicFileNameDTO.getNewMusicFileName());
        if (musicFile == null) {
            throw new NoSuchElementException("Music file with name " + updateMusicFileNameDTO.getCurrentMusicFileName() + " not found");
        }
        if (musicFileNewName) {
            throw new IllegalArgumentException("This music name " + updateMusicFileNameDTO.getNewMusicFileName() + " already exists");
        } else {
            musicFile.setMusicFileName(updateMusicFileNameDTO.getNewMusicFileName());
            musicFileRepository.save(musicFile);
        }
    }

    public void updateMusicFileSubscription(UpdateMusicFileSubscriptionDTO updateMusicFileSubscriptionDTO) {
        MusicFile musicFile = musicFileRepository.findByMusicFileNameIgnoreCase(updateMusicFileSubscriptionDTO.getMusicFileName());
        if (musicFile == null) {
            throw new NoSuchElementException("Music file with name " + updateMusicFileSubscriptionDTO.getMusicFileName() + " not found");
        }
        Subscription subscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(updateMusicFileSubscriptionDTO.getNewMusicFileSubscription());
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + updateMusicFileSubscriptionDTO.getNewMusicFileSubscription() + " not found");
        }
        musicFile.setSubscription(subscription);
        musicFileRepository.save(musicFile);
    }

    public void transferMusicFilesToAnotherSubscription(TransferMusicFilesToAnotherSubscription transferMusicFilesToAnotherSubscription) {
        Subscription currentSubscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(transferMusicFilesToAnotherSubscription.getCurrentMusicFilesSubscription());
        if (currentSubscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + transferMusicFilesToAnotherSubscription.getCurrentMusicFilesSubscription() + " not found");
        }
        Subscription newSubscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(transferMusicFilesToAnotherSubscription.getNewMusicFileSubscription());
        if (newSubscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + transferMusicFilesToAnotherSubscription.getNewMusicFileSubscription() + " not found");
        }
        List<MusicFile> musicFiles = currentSubscription.getMusicFiles().stream()
                .peek(musicFile -> musicFile.setSubscription(newSubscription))
                .collect(Collectors.toList());

        musicFileRepository.saveAll(musicFiles);
    }

    public void deleteAllMusicFiles() {
        musicFileRepository.deleteAll(findAllMusicFiles());
    }

    public void deleteMusicFilesByMusicFileName(String musicFileName) {
        MusicFile musicFile = musicFileRepository.findByMusicFileNameIgnoreCase(musicFileName);
        if (musicFile == null) {
            throw new NoSuchElementException("Music file with name " + musicFileName + " not found");
        }
        musicFileRepository.delete(musicFile);
    }

    public void deleteMusicFilesById(long musicFileId) {
        MusicFile musicFile = musicFileRepository.findById(musicFileId);
        if (musicFile == null) {
            throw new NoSuchElementException("Music file with id " + musicFileId + " not found");
        }
        musicFileRepository.delete(musicFile);
    }

    public void deleteMusicFilesBySubscription(String subscriptionName) {
        Subscription subscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(subscriptionName);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with name " + subscriptionName + " not found");
        }
        List<MusicFile> musicFiles = subscription.getMusicFiles();
        musicFileRepository.deleteAll(musicFiles);
    }

    public List<MusicFile> findMusicFileBySubscription(String subscriptionName) {
        Subscription subscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(subscriptionName);
        Subscription freeSubscription = subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE");
        Subscription optimalSubscription = subscriptionRepository.findBySubscriptionNameIgnoreCase("OPTIMAL");
        Subscription maximumSubscription = subscriptionRepository.findBySubscriptionNameIgnoreCase("MAXIMUM");
        List<MusicFile> musicFileList = new ArrayList<>();
        switch (subscription.getSubscriptionName().toUpperCase()) {
            case "FREE":
                musicFileList.addAll(freeSubscription.getMusicFiles());
                break;
            case "OPTIMAL":
                musicFileList.addAll(freeSubscription.getMusicFiles());
                musicFileList.addAll(optimalSubscription.getMusicFiles());
                break;
            case "MAXIMUM":
                musicFileList.addAll(freeSubscription.getMusicFiles());
                musicFileList.addAll(optimalSubscription.getMusicFiles());
                musicFileList.addAll(maximumSubscription.getMusicFiles());
                break;
            default:
                throw new NoSuchElementException("Subscription with name " + subscriptionName + " not found");
        }
        return musicFileList;
    }
}