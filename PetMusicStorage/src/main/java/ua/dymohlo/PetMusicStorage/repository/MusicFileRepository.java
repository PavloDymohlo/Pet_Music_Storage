package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;

import java.util.List;

public interface MusicFileRepository extends JpaRepository<MusicFile, Long> {
    boolean existsByMusicFileName(String musicFileName);

    List<MusicFile> findAll();

    MusicFile findById(long musicFileId);

    MusicFile findByMusicFileName(String musicFileName);

}
