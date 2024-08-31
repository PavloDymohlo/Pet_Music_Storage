package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.service.MusicFileService;

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
    public ResponseEntity<Object> listFreeSubscription(@RequestParam("subscriptionName")
                                                       String subscriptionName) {
        try {
            List<MusicFile> musicFiles = musicFileService.findMusicFileBySubscription(subscriptionName);
            log.info("Music files with subscription " + subscriptionName + " found successful");
            return ResponseEntity.ok(musicFiles);
        }catch (NoSuchElementException e){
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e){
            log.error("Error finding all subscriptions");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
