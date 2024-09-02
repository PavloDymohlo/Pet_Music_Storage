package ua.dymohlo.PetMusicStorage.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.service.SubscriptionService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {
    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @Scheduled(fixedRate = 60000)
    public void checkSubscriptionExpiration() {
        List<User> users = userService.findAllUsers();
        users.removeIf(user -> "ADMIN".equals(user.getSubscription().getSubscriptionName()));
        users.removeIf(user -> "FREE".equals(user.getSubscription().getSubscriptionName()));
        users.stream()
                .filter(user -> {
                    LocalDateTime endTime = user.getEndTime();
                    return endTime != null && endTime.isBefore(LocalDateTime.now());
                })
                .peek(user -> log.info("Subscription expired for user with ID {}", user.getId()))
                .forEach(user -> subscriptionService.autoRenewSubscriptionForUser(user));
        log.info("Subscription expiration check completed.");
    }
}