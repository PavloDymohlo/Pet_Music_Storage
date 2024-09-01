package ua.dymohlo.PetMusicStorage.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class OptimalSubscriptionPageController {

    @GetMapping("/optimal_subscription")
    public String showOptimalSubscriptionPage() {
        log.info("Optimal subscription page accessed.");
        return "pages/optimal_subscription_music_page";
    }
}