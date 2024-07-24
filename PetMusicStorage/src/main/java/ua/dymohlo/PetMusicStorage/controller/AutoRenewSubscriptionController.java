package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.PetMusicStorage.Enum.AutoRenewStatus;
import ua.dymohlo.PetMusicStorage.dto.TransactionDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateSubscriptionDTO;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auto_renew_subscription")
public class AutoRenewSubscriptionController {
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentController paymentController;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<String> autoRenewSubscription(@RequestBody User user) {
        try {
            if (user.getAutoRenew().equals(AutoRenewStatus.YES)) {
                Subscription subscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(user.getSubscription().getSubscriptionName());
                TransactionDTO transactionDTO = TransactionDTO.builder()
                        .outputCardNumber(user.getUserBankCard().getCardNumber())
                        .sum(subscription.getSubscriptionPrice())
                        .cardExpirationDate(user.getUserBankCard().getCardExpirationDate())
                        .cvv(user.getUserBankCard().getCvv()).build();
                ResponseEntity<String> paymentResponse = paymentController.payment(transactionDTO);
                if (paymentResponse.getStatusCode().is2xxSuccessful()) {
                    UpdateSubscriptionDTO updateSubscriptionDTO = UpdateSubscriptionDTO.builder()
                            .newSubscription(subscription).build();
                    userService.updateSubscription(user.getPhoneNumber(), updateSubscriptionDTO);
                    log.info("Subscription for user with phone number {} updated successful", user.getPhoneNumber());
                    String responseMessage = "Subscription for user with phone number " + user.getPhoneNumber() + " updated successful";
                    return ResponseEntity.ok(responseMessage);
                } else if (paymentResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    userService.setFreeSubscription(user.getPhoneNumber());
                    String errorMessage = paymentResponse.getBody();
                    log.warn(errorMessage);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                }
            } else {
                userService.setFreeSubscription(user.getPhoneNumber());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment failed");
            }
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }
}