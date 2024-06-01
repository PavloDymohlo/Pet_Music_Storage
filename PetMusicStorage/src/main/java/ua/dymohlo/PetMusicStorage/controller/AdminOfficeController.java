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

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin_office")
public class AdminOfficeController {
    private final UserService userService;
    private final JWTService jwtService;
    private final DatabaseUserDetailsService databaseUserDetailsService;
    private final PaymentController paymentController;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;


    @PutMapping("/update_phone_number")
    public ResponseEntity<String> updateUserPhoneNumber(@RequestBody UpdatePhoneNumberDTO request) {
        try {
            userService.updatePhoneNumber(request.getCurrentPhoneNumber(), request.getNewPhoneNumber());
            UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getNewPhoneNumber()));
            String newJwtToken = jwtService.generateJwtToken(userDetails);
            log.info("Phone number {} updated successfully!", request.getNewPhoneNumber());
            return ResponseEntity.ok().body(newJwtToken);
        } catch (IllegalArgumentException e) {
            log.warn("Phone number {} already exists", request.getNewPhoneNumber());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating phone number", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_bank_card")
    public ResponseEntity<String> updateUserBankCard(@RequestBody UpdateUserBankCardDTO request) {
        try {
            userService.updateBankCard(request.getUserPhoneNumber(), request);
            log.info("Bank card for user with phone number {} updated successful", request.getUserPhoneNumber());
            return ResponseEntity.ok("Bank card for user with phone number " + request.getUserPhoneNumber() + " updated successful");
        } catch (IllegalArgumentException e) {
            log.warn("Invalid card details for user with phone number {}", request.getUserPhoneNumber());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating bank card");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_password")
    public ResponseEntity<String> updateUserPassword(@RequestBody UpdatePasswordDTO request) {
        try {
            userService.updatePassword(request.getUserPhoneNumber(), request);
            log.info("Password for user with phone number {} updated successful", request.getUserPhoneNumber());
            return ResponseEntity.ok("Password for user with phone number " + request.getUserPhoneNumber() + " updated successful");
        } catch (IllegalArgumentException e) {
            log.warn("Phone number {} not found", request.getUserPhoneNumber());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating password");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_email")
    public ResponseEntity<String> updateUserEmail(@RequestBody UpdateEmailDTO request) {
        try {
            userService.updateEmail(request.getUserPhoneNumber(), request);
            log.info("Email for user with phone number {} updated successful", request.getUserPhoneNumber());
            return ResponseEntity.ok("Email for user with phone number " + request.getUserPhoneNumber() + " updated successful");
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating email");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/set_auto_renew")
    public ResponseEntity<String> setUserAutoRenewStatus(@RequestBody SetAutoRenewDTO request) {
        try {
            userService.setAutoRenewStatus(request.getUserPhoneNumber(), request);
            log.info("Auto renew status for user with phone number {} set successfully for user with phone number", request.getUserPhoneNumber());
            return ResponseEntity.ok("Auto renew status for user with phone number " + request.getUserPhoneNumber() + " set successfully");
        } catch (IllegalArgumentException e) {
            log.warn("Phone number {} not found", request.getUserPhoneNumber());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while set auto renew status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_subscription")
    public ResponseEntity<String> updateUserSubscription(@RequestBody UpdateSubscriptionDTO request) {
        try {
            User user = userRepository.findByPhoneNumber(request.getUserPhoneNumber());
            Subscription subscription = subscriptionRepository.findBySubscriptionName(request.getNewSubscription().getSubscriptionName());
            TransactionDTO transactionDTO = TransactionDTO.builder()
                    .outputCardNumber(user.getUserBankCard().getCardNumber())
                    .sum(subscription.getSubscriptionPrice())
                    .cardExpirationDate(user.getUserBankCard().getCardExpirationDate())
                    .cvv(user.getUserBankCard().getCvv()).build();
            ResponseEntity<String> paymentResponse = paymentController.payment(transactionDTO);
            if (paymentResponse.getStatusCode().is2xxSuccessful()) {
                userService.updateSubscription(request.getUserPhoneNumber(), request);
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

    @GetMapping("/users")
    public ResponseEntity<List<User>> findAllUsers() {
        try {
            List<User> users = userService.findAllUsers();
            log.info("Fetched all user successful");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("An error occurred while fetching all users");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user_by_phone")
    public ResponseEntity<Object> findUserByPhoneNumber(@RequestParam("phoneNumber") long phoneNumber) {
        try {
            User user = userService.findUserByPhoneNumber(phoneNumber);
            if (user != null) {
                log.info("Fetched user by phone number successful.");
                return ResponseEntity.ok(user);
            } else {
                log.warn("user with phone number {} not found", phoneNumber);
                String errorMessage = "User with phone number " + phoneNumber + " not found";
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
        } catch (Exception e) {
            log.error("Error finding user by phone number", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_bank_card")
    public ResponseEntity<List<?>> findUserByBankCard(@RequestParam("bankCardNumber") long bankCardNumber) {
        try {
            List<User> users = userService.findUserByBankCard(bankCardNumber);
            if (users != null) {
                log.info("Fetched users by bank card number successful");
                return ResponseEntity.ok(users);
            } else {
                log.warn("Users with bank card {} not found", bankCardNumber);
                String errorMessage = "Users with bank card number " + bankCardNumber + " not found";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonList(errorMessage));
            }
        } catch (Exception e) {
            log.error("Error finding user by bank card", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_subscription")
    public ResponseEntity<List<?>> findUserBySubscription(@RequestParam("subscription") String userSubscription) {
        try {
            List<User> users = userService.findUserBySubscription(userSubscription);
            if (users != null) {
                log.info("Fetched users by subscription successful");
                return ResponseEntity.ok(users);
            } else {
                log.warn("Users with subscription :{} not found ", userSubscription);
                String errorMessage = "Users with subscription: " + userSubscription + " not found";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonList(errorMessage));
            }
        } catch (IllegalArgumentException e) {
            log.error("Subscription not found: {}", userSubscription, e);
            String subscriptionErrorMessage = "Subscription " + userSubscription + " not found";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonList(subscriptionErrorMessage));
        } catch (Exception e) {
            log.error("Error finding user by subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_email")
    public ResponseEntity<Object> findUserByEmail(@RequestParam("email") String userEmail) {
        try {
            User user = userService.findUserByEmail(userEmail);
            if (user != null) {
                log.info("Fetched users by email successful");
                return ResponseEntity.ok(user);
            } else {
                log.warn("User with email :{} not found", userEmail);
                String errorMessage = "User with email " + userEmail + " not found";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
        } catch (Exception e) {
            log.error("Error finding user by email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_id")
    public ResponseEntity<Object> findUserById(@RequestParam("id") long userId) {
        try {
            User user = userService.findUserById(userId);
            if (user != null) {
                log.info("Fetched users by id successful");
                return ResponseEntity.ok(user);
            } else {
                log.warn("User with id :{} not found", userId);
                String errorMessage = "User with id " + userId + " not found";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
        } catch (Exception e) {
            log.error("Error finding user by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}