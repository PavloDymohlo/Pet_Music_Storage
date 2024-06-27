package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import ua.dymohlo.PetMusicStorage.dto.MusicFileDTO;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.service.MusicFileService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/music_files")
public class MusicFileController {
    private final MusicFileService musicFileService;
    @GetMapping
    public ModelAndView adminOfficePage() {
        return new ModelAndView("pages/music_page");
    }

}
