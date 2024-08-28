package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ua.dymohlo.PetMusicStorage.dto.*;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class AllUsersPageController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentController paymentController;

    @GetMapping
    public ModelAndView adminOfficePage() {
        return new ModelAndView("pages/users");
    }

    @GetMapping("/all_users")
    public ResponseEntity<?> findAllUsers() {
        try {
            List<User> users = userService.findAllUsers();
            log.info("Fetched all user successful");
            return ResponseEntity.ok(users);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while fetching all users");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user_by_phone")
    public ResponseEntity<Object> findUserByPhoneNumber(@RequestParam("phoneNumber") long phoneNumber) {
        try {
            User user = userService.findUserByPhoneNumber(phoneNumber);
            log.info("Fetched user with phone number {} successful", phoneNumber);
            return ResponseEntity.ok(user);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by phone number", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_email")
    public ResponseEntity<Object> findUserByEmail(@RequestParam("email") String userEmail) {
        try {
            User user = userService.findUserByEmail(userEmail);
            log.info("Fetched users by email successful");
            return ResponseEntity.ok(user);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_bank_card")
    public ResponseEntity<?> findUserByBankCard(@RequestParam("bankCardNumber") long bankCardNumber) {
        try {
            List<User> users = userService.findUserByBankCard(bankCardNumber);
            log.info("Fetched users by bank card number successful");
            return ResponseEntity.ok(users);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by bank card", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_id")
    public ResponseEntity<Object> findUserById(@RequestParam("id") long userId) {
        try {
            User user = userService.findUserById(userId);
            log.info("Fetched users by id successful");
            return ResponseEntity.ok(user);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user_by_subscription")
    public ResponseEntity<?> findUserBySubscription(@RequestParam("subscription") String userSubscription) {
        try {
            List<User> users = userService.findUserBySubscription(userSubscription);
            log.info("Fetched users by subscription successful");
            System.out.println("success");
            return ResponseEntity.ok(users);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update_phone_number")
    public ResponseEntity<String> updateUserPhoneNumber(@RequestBody UpdatePhoneNumberDTO request) {
        try {
            userService.updatePhoneNumber(request.getCurrentPhoneNumber(), request.getNewPhoneNumber());
            String responseMessage = "New phone number " + request.getNewPhoneNumber() + " updated successfully!";
            log.info("Phone number {} updated successfully!", request.getNewPhoneNumber());
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
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

    @PutMapping("/update_password")
    public ResponseEntity<String> updateUserPassword(@RequestBody UpdatePasswordDTO request) {
        try {
            userService.updatePassword(request.getUserPhoneNumber(), request);
            log.info("Password for user with phone number {} updated successful", request.getUserPhoneNumber());
            String responseMessage = "Password for user with phone number " + request.getUserPhoneNumber() + " updated successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating password");
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
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while set auto renew status");
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
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating email");
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
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            String responseMessage = "An error occurred while updating bank card";
            log.error("An error occurred while updating bank card");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
        }
    }

    @PutMapping("/update_subscription")
    public ResponseEntity<String> updateUserSubscription(@RequestBody UpdateSubscriptionDTO request) {
        try {
            User user = userRepository.findByPhoneNumber(request.getUserPhoneNumber());
            Subscription subscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(request.getNewSubscription().getSubscriptionName());
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
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
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
    public ResponseEntity<String> deleteUserByPhoneNumber(@RequestBody String userPassword,
                                                          @RequestParam("phoneNumber") long phoneNumber) {
        try {
            userService.deleteUserByPhoneNumber(phoneNumber, userPassword);
            log.info("User with phoneNumber {} delete successful", phoneNumber);
            String responseMessage = "User with phoneNumber " + phoneNumber + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.error("User with phone number {} not found {}", phoneNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting a user by phone number", e);
            String errorMessage = "Error deleting user with phone number " + phoneNumber;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
}
