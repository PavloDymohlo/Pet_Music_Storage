package ua.dymohlo.PetMusicStorage.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class FreeSubscriptionPageController {

    @GetMapping("/free_subscription")
    public String showFreeSubscriptionPage(){
        log.info("Free subscription page accessed.");
        return "pages/free_subscription_music_page";
    }
}
