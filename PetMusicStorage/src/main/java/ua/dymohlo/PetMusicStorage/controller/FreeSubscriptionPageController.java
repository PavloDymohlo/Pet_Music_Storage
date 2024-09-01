package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.service.MusicFileService;
import org.springframework.core.io.Resource;
import java.nio.file.Path;
import java.nio.file.Paths;



import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/free_subscription")
public class FreeSubscriptionPageController {
    private final MusicFileService musicFileService;

    @GetMapping
    public ModelAndView showFreeSubscriptionPage() {
        log.info("Free subscription page accessed.");
        return new ModelAndView("pages/free_subscription_music_page");
    }

    @GetMapping("/list_free_subscription")
    public ResponseEntity<Resource> listFreeSubscription(@RequestParam("subscriptionName") String subscriptionName) {
        try {
            MusicFile musicFile = musicFileService.findMusicFileBySubscription(subscriptionName).get(0);
            Path filePath = Paths.get(musicFile.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(resource);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Error finding music file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

//    @GetMapping("/list_free_subscription")
//    public ResponseEntity<List<MusicFile>> listFreeSubscription(@RequestParam("subscriptionName") String subscriptionName) {
//        try {
//            List<MusicFile> musicFiles = musicFileService.findMusicFileBySubscription(subscriptionName);
//            if (musicFiles.isEmpty()) {
//                return ResponseEntity.noContent().build();
//            }
//            return ResponseEntity.ok(musicFiles);
//        } catch (NoSuchElementException e) {
//            log.warn(e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        } catch (Exception e) {
//            log.error("Error finding music files");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }

}