package ua.dymohlo.PetMusicStorage.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class MaximumSubscriptionPageController {

    @GetMapping("/maximum_subscription")
    public String showMaximumSubscriptionPage(){
        log.info("Maximum subscription page accessed.");
        return "pages/maximum_subscription_music_page";
    }
}
