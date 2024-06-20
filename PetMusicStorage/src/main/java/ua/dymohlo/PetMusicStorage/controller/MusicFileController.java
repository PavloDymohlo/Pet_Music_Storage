package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.PetMusicStorage.dto.MusicFileDTO;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.service.MusicFileService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MusicFileController {
    private final MusicFileService musicFileService;

    @GetMapping("/music_files")
    public List<MusicFileDTO> getAllMusicFiles() {
        List<MusicFile> musicFiles = musicFileService.findAllMusicFiles();
        return musicFiles.stream()
                .map(musicFile -> new MusicFileDTO(musicFile.getMusicFileName(), musicFile.getFilePath()))
                .collect(Collectors.toList());
    }
}
