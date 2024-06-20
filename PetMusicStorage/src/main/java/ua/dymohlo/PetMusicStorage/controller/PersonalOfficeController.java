package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ua.dymohlo.PetMusicStorage.dto.*;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.SubscriptionService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/personal_office")
public class PersonalOfficeController {
    private final UserService userService;
    private final JWTService jwtService;
    private final DatabaseUserDetailsService databaseUserDetailsService;
    private final PaymentController paymentController;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    @GetMapping
    public ModelAndView personalOfficePage() {
        return new ModelAndView("pages/personal_office");
    }

    @PutMapping("/update_phone_number")
    public ResponseEntity<String> updatePhoneNumber(@RequestBody UpdatePhoneNumberDTO request,
                                                    @RequestHeader("Authorization") String jwtToken) {
        long currentUserPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", currentUserPhoneNumber);
        try {
            userService.updatePhoneNumber(currentUserPhoneNumber, request.getNewPhoneNumber());
            UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getNewPhoneNumber()));
            String newJwtToken = jwtService.generateJwtToken(userDetails);
            log.info("Phone number {} updated successfully!", request.getNewPhoneNumber());
            return ResponseEntity.ok().body(newJwtToken);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_bank_card")
    public ResponseEntity<String> updateBankCard(@RequestBody UpdateUserBankCardDTO request,
                                                 @RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            userService.updateBankCard(userPhoneNumber, request);
            log.info("Bank card for user with phone number {} updated successful", request.getUserPhoneNumber());
            String responseMessage = "Bank card for user with phone number " + userPhoneNumber + " updated successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating bank card", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordDTO request,
                                                 @RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            userService.updatePassword(userPhoneNumber, request);
            log.info("Password for user with phone number {} updated successful", request.getUserPhoneNumber());
            String responseMessage = "Password for user with phone number " + userPhoneNumber + " updated successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating bank card", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_email")
    public ResponseEntity<String> updateEmail(@RequestBody UpdateEmailDTO request,
                                              @RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            userService.updateEmail(userPhoneNumber, request);
            log.info("Email for user with phone number {} updated successful", request.getUserPhoneNumber());
            String responseMessage = "Email for user with phone number " + userPhoneNumber + " updated successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/set_auto_renew")
    public ResponseEntity<String> setAutoRenewStatus(@RequestBody SetAutoRenewDTO request,
                                                     @RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            userService.setAutoRenewStatus(userPhoneNumber, request);
            log.info("Auto renew status for user with phone number {} set successfully", request.getUserPhoneNumber());
            String responseMessage = "Auto renew status for user with phone number " + userPhoneNumber + " set successfully";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while set auto renew", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_subscription")
    public ResponseEntity<String> updateSubscription(@RequestBody UpdateSubscriptionDTO request,
                                                     @RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            User user = userRepository.findByPhoneNumber(userPhoneNumber);
            Subscription subscription = subscriptionRepository.findBySubscriptionName(request.getNewSubscription().getSubscriptionName());
            TransactionDTO transactionDTO = TransactionDTO.builder()
                    .outputCardNumber(user.getUserBankCard().getCardNumber())
                    .sum(subscription.getSubscriptionPrice())
                    .cardExpirationDate(user.getUserBankCard().getCardExpirationDate())
                    .cvv(user.getUserBankCard().getCvv()).build();
            ResponseEntity<String> paymentResponse = paymentController.payment(transactionDTO);
            if (paymentResponse.getStatusCode().is2xxSuccessful()) {
                userService.updateSubscription(userPhoneNumber, request);
                log.info("Subscription for user with phone number {} updated successful", user.getPhoneNumber());
                String responseMessage = "Subscription for user with phone number " + request.getUserPhoneNumber() + " updated successful";
                return ResponseEntity.ok(responseMessage);
            } else if (paymentResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String errorMessage = paymentResponse.getBody();
                log.warn("Payment failed: {}", errorMessage);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment failed");
            }
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> findAllSubscriptions() {
        try {
            List<Subscription> subscriptions = subscriptionService.findAllSubscription();
            subscriptions.remove(subscriptionRepository.findBySubscriptionName("ADMIN"));
            subscriptions.remove(subscriptionRepository.findBySubscriptionName("REGISTRATION"));
            log.info("Fetched all subscription successful");
            return ResponseEntity.ok(subscriptions);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding all subscriptions");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/subscription_by_price")
    public ResponseEntity<?> findSubscriptionsByPrice(@RequestBody FindSubscriptionsByPriceDTO request) {
        try {
            List<Subscription> subscriptions = subscriptionService.findSubscriptionsByPrice(request.getMinPrice(),
                    request.getMaxPrice());
            subscriptions.removeIf(subscription -> "ADMIN".equals(subscription.getSubscriptionName()));
            subscriptions.removeIf(subscription -> "REGISTRATION".equals(subscription.getSubscriptionName()));

            log.info("Fetched subscription between price " + request.getMinPrice() + " and " + request.getMaxPrice());
            return ResponseEntity.ok(subscriptions);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding subscription between price " + request.getMinPrice() + " and " + request.getMaxPrice());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/subscription_by_name")
    public ResponseEntity<?> findSubscriptionBySubscriptionName(@RequestParam("subscriptionName") String subscriptionName) {
        try {
            Subscription subscription = subscriptionService.findSubscriptionBySubscriptionName(subscriptionName);
            if ("ADMIN".equals(subscription.getSubscriptionName()) || "REGISTRATION".equals(subscription.getSubscriptionName())) {
                throw new NoSuchElementException("Subscription with subscriptionName " + subscriptionName + " not found");
            }
            log.info("Fetched subscription by subscriptionName");
            return ResponseEntity.ok(subscription);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding subscription by subscriptionName");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}