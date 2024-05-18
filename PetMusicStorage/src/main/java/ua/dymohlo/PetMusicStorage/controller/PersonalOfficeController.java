package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.PetMusicStorage.dto.*;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

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


    @PutMapping("/updatePhoneNumber")
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
        } catch (IllegalArgumentException e) {
            log.error("Failed to update phone number: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updateBankCard")
    public ResponseEntity<String> updateBankCard(@RequestBody UpdateUserBankCardDTO request,
                                                 @RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            userService.updateBankCard(userPhoneNumber, request);
            log.info("Bank card for user with phone number {} updated successful", request.getUserPhoneNumber());
            return ResponseEntity.ok("Bank card for user with phone number " + request.getUserPhoneNumber() + " updated successful");
        } catch (IllegalArgumentException e) {
            log.warn("Invalid card details for user with phone number {}", request.getUserPhoneNumber());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating bank card", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updatePassword")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordDTO request,
                                                 @RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            userService.updatePassword(userPhoneNumber, request);
            log.info("Password for user with phone number {} updated successful", request.getUserPhoneNumber());
            return ResponseEntity.ok("Password for user with phone number " + request.getUserPhoneNumber() + " updated successful");
        } catch (Exception e) {
            log.error("An error occurred while updating bank card", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updateEmail")
    public ResponseEntity<String> updateEmail(@RequestBody UpdateEmailDTO request,
                                              @RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            userService.updateEmail(userPhoneNumber, request);
            log.info("Email for user with phone number {} updated successful", request.getUserPhoneNumber());
            return ResponseEntity.ok("Email for user with phone number " + request.getUserPhoneNumber() + " updated successful");
        } catch (IllegalArgumentException e) {
            log.warn("Email {} is already exists", request.getNewEmail());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/setAutoRenew")
    public ResponseEntity<String> setAutoRenewStatus(@RequestBody SetAutoRenewDTO request,
                                                     @RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            userService.setAutoRenewStatus(userPhoneNumber, request);
            log.info("Auto renew status for user with phone number {} set successfully", request.getUserPhoneNumber());
            return ResponseEntity.ok("Auto renew status for user with phone number " + request.getUserPhoneNumber() + " set successfully");
        } catch (IllegalArgumentException e) {
            log.warn("Phone number {} not found", request.getUserPhoneNumber());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while set auto renew", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updateSubscription")
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
                return ResponseEntity.ok("Subscription for user with phone number " + request.getUserPhoneNumber() + " updated successful");
            } else if (paymentResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String errorMessage = paymentResponse.getBody();
                log.warn("Payment failed: {}", errorMessage);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment failed");
            }
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}