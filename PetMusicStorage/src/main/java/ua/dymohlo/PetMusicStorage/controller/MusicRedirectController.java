package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/music")
@Slf4j
@RequiredArgsConstructor
public class MusicRedirectController {

    private final UserService userService;

    @GetMapping("/get_music_page")
    public ResponseEntity<?> getMusicPage(@RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        User user = userService.findUserByPhoneNumber(userPhoneNumber);
        Subscription subscription = user.getSubscription();
        String subscriptionName = subscription.getSubscriptionName().toUpperCase();

        String redirectUrl;
        switch (subscriptionName) {
            case "FREE":
                redirectUrl = "/free_subscription";
                break;
            case "OPTIMAL":
                redirectUrl = "/optimal_subscription";
                break;
            case "MAXIMUM":
                redirectUrl = "/maximum_subscription";
                break;
            case "ADMIN":
                redirectUrl = "/maximum_subscription";
                break;
            default:
                redirectUrl = "/host_page";
        }

        log.info("User with phone number {} redirected to {}", userPhoneNumber, redirectUrl);
        Map<String, String> response = new HashMap<>();
        response.put("url", redirectUrl);
        return ResponseEntity.ok(response);
    }
}