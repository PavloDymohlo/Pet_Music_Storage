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
import ua.dymohlo.PetMusicStorage.service.SubscriptionService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

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
    private final SubscriptionService subscriptionService;


    @PutMapping("/update_phone_number")
    public ResponseEntity<String> updateUserPhoneNumber(@RequestBody UpdatePhoneNumberDTO request) {
        try {
            userService.updatePhoneNumber(request.getCurrentPhoneNumber(), request.getNewPhoneNumber());
            UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getNewPhoneNumber()));
            String newJwtToken = jwtService.generateJwtToken(userDetails);
            log.info("Phone number {} updated successfully!", request.getNewPhoneNumber());
            return ResponseEntity.ok().body(newJwtToken);
        }catch (NoSuchElementException e){
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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
            String responseMessage = "Bank card for user with phone number " + request.getUserPhoneNumber() + " updated successful";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
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
            String responseMessage = "Password for user with phone number " + request.getUserPhoneNumber() + " updated successful";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
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
            String responseMessage = "Email for user with phone number " + request.getUserPhoneNumber() + " updated successful";
            return ResponseEntity.ok(responseMessage);
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
            String responseMessage = "Auto renew status for user with phone number " + request.getUserPhoneNumber() + " set successfully";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
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
                String responseMessage = "Subscription for user with phone number " + request.getUserPhoneNumber() + " updated successful";
                return ResponseEntity.ok(responseMessage);
            } else if (paymentResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String errorMessage = paymentResponse.getBody();
                log.warn(errorMessage);
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
            log.info("Fetched user by phone number successful.");
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by phone number", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_bank_card")
    public ResponseEntity<?> findUserByBankCard(@RequestParam("bankCardNumber") long bankCardNumber) {
        try {
            List<User> users = userService.findUserByBankCard(bankCardNumber);
            log.info("Fetched users by bank card number successful");
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by bank card", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_subscription")
    public ResponseEntity<?> findUserBySubscription(@RequestParam("subscription") String userSubscription) {
        try {
            List<User> users = userService.findUserBySubscription(userSubscription);
            log.info("Fetched users by subscription successful");
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_email")
    public ResponseEntity<Object> findUserByEmail(@RequestParam("email") String userEmail) {
        try {
            User user = userService.findUserByEmail(userEmail);
            log.info("Fetched users by email successful");
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_id")
    public ResponseEntity<Object> findUserById(@RequestParam("id") long userId) {
        try {
            User user = userService.findUserById(userId);
            log.info("Fetched users by id successful");
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete_user_by_id")
    public ResponseEntity<String> deleteUserById(@RequestParam("id") long userId) {
        try {
            userService.deleteUserById(userId);
            log.info("User with id :{} deleted successful", userId);
            String responseMessage = "User with id " + userId + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.error("User with id {} not found: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting a user by id", e);
            String errorMessage = "Error deleting user with id " + userId;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @DeleteMapping("/delete_all_users")
    public ResponseEntity<String> deleteAllUsers(@RequestHeader("Authorization") String jwtToken) {
        try {
            long currentAdminPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
            userService.deleteAllUsers(currentAdminPhoneNumber);
            log.info("All users deleted successful");
            String responseMessage = "All users deleted successful";
            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            String errorMessage = "Error deleting all users: ";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMessage + e.getMessage());
        }
    }

    @DeleteMapping("/delete_user_by_phone_number")
    public ResponseEntity<String> deleteUserByPhoneNumber(@RequestParam("phoneNumber") long phoneNumber) {
        try {
            userService.deleteUserByPhoneNumber(phoneNumber);
            log.info("User with phoneNumber {} delete successful", phoneNumber);
            String responseMessage = "User with phoneNumber " + phoneNumber + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.error("User with phone number {} not found {}", phoneNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting a user by phone number", e);
            String errorMessage = "Error deleting user with phone number " + phoneNumber;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @DeleteMapping("/delete_users_by_bank_card_number")
    public ResponseEntity<String> deleteUsersByBankCardNumber(@RequestParam("bankCardNumber") long bankCardNumber,
                                                              @RequestHeader("Authorization") String jwtToken) {
        try {
            long currentAdminPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
            userService.deleteUserByBankCardNumber(bankCardNumber, currentAdminPhoneNumber);
            log.info("Users with bankCardNumber {} delete successful", bankCardNumber);
            String responseMessage = "Users with bankCardNumber " + bankCardNumber + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting users by bank card number", e);
            String errorMessage = "Error deleting users with bank card number " + bankCardNumber;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @DeleteMapping("/delete_users_by_subscription")
    public ResponseEntity<String> deleteUsersBySubscription(@RequestParam("subscription") String subscription,
                                                            @RequestHeader("Authorization") String jwtToken) {
        try {
            long currentAdminPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
            userService.deleteUsersBySubscription(currentAdminPhoneNumber, subscription);
            log.info("Users with subscription {} delete successful", subscription);
            String responseMessage = "Users with subscription " + subscription + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting users by subscription ", e);
            String errorMessage = "Error deleting users with subscription " + subscription;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @DeleteMapping("/delete_user_by_email")
    public ResponseEntity<String> deleteUserByEmail(@RequestParam("email") String userEmail) {
        try {
            userService.deleteUserByEmail(userEmail);
            log.info("User with email {} delete successful", userEmail);
            String responseMessage = "User with email " + userEmail + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting user by email ", e);
            String errorMessage = "Error deleting user with email " + userEmail;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @PostMapping("/create_new_subscription")
    public ResponseEntity<Object> createNewSubscription(@RequestBody NewSubscriptionDTO request) {
        try {
            subscriptionService.addNewSubscription(request);
            log.info("New subscription {} add in data base", request.getSubscriptionName());
            String responseMessage = " New subscription " + request.getSubscriptionName() + " add in data base";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating new subscription", e);
            String errorMessage = "Error creating new subscription" + request.getSubscriptionName();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @GetMapping("/subscription_by_id")
    public ResponseEntity<Object> findSubscriptionById(@RequestParam("id") long subscriptionId) {
        try {
            Subscription subscription = subscriptionService.findSubscriptionById(subscriptionId);
            log.info("fetched subscription by id successful");
            return ResponseEntity.ok(subscription);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding subscription by id");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> findAllSubscriptions() {
        try {
            List<Subscription> subscriptions = subscriptionService.findAllSubscription();
            log.info("Fetched all subscription");
            return ResponseEntity.ok(subscriptions);
        } catch (IllegalArgumentException e) {
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
            log.info("Fetched subscriptions by price");
            return ResponseEntity.ok(subscriptions);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding subscription by price");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/subscription_by_name")
    public ResponseEntity<Object> findSubscriptionBySubscriptionName(@RequestParam("subscriptionName") String subscriptionName) {
        try {
            Subscription subscription = subscriptionService.findSubscriptionBySubscriptionName(subscriptionName);
            log.info("Fetched subscription by subscriptionName");
            return ResponseEntity.ok(subscription);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding subscription by subscriptionName");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update_subscription_name")
    public ResponseEntity<String> updateSubscriptionName(@RequestBody UpdateSubscriptionNameDTO request) {
        try {
            subscriptionService.updateSubscriptionName(request);
            log.info("Subscription with new subscriptionName {} updated successful", request.getNewSubscriptionName());
            String responseMessage = "Subscription with new subscriptionName " + request.getNewSubscriptionName() + " updated successful";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while subscriptionName updated");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_subscription_price")
    public ResponseEntity<String> updateSubscriptionPrice(@RequestBody UpdateSubscriptionPriceDTO request) {
        try {
            subscriptionService.updateSubscriptionPrice(request);
            log.info("Subscription with subscriptionName {} has updated price", request.getSubscriptionName());
            String responseMessage = "Subscription with subscriptionName " + request.getSubscriptionName() + " has updated price";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while subscriptionPrice updated");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_subscription_duration_time")
    public ResponseEntity<String> updateSubscriptionDurationTime(@RequestBody UpdateSubscriptionDurationTimeDTO request) {
        try {
            subscriptionService.updateSubscriptionDurationTime(request);
            log.info("Subscription with subscriptionName {} has updated duration time", request.getSubscriptionName());
            String responseMessage = "Subscription with subscriptionName " + request.getSubscriptionName() + " has updated duration time";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while subscription duration time updated");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }@DeleteMapping("/delete_subscription_by_id")
    public ResponseEntity<String> deleteSubscriptionById(@RequestParam("id")long subscriptionId){
        try{
            subscriptionService.deleteSubscriptionById(subscriptionId);
            log.info("Subscription with id {} delete successful",subscriptionId);
            String responseMessage = "Subscription with id "+subscriptionId+" delete successful";
            return ResponseEntity.ok(responseMessage);
        }catch (IllegalArgumentException e){
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}