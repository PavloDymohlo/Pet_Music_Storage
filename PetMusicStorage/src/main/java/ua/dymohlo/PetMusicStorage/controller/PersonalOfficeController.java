package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
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
                                                    @RequestHeader("Authorization") String jwtToken,
                                                    HttpServletResponse response) {
        long currentUserPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", currentUserPhoneNumber);
        try {
            userService.updatePhoneNumber(currentUserPhoneNumber, request.getNewPhoneNumber());
            UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getNewPhoneNumber()));
            String responseMessage = "Phone number updated successfully!";
            String newJwtToken = jwtService.generateJwtToken(userDetails);
            log.info("New jwt token for users with phone number {} : {}", request.getNewPhoneNumber(), newJwtToken);
            log.info("Phone number {} updated successfully!", request.getNewPhoneNumber());
            ResponseCookie jwtCookie = ResponseCookie.from("JWT_TOKEN", newJwtToken)
                    .httpOnly(false)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofHours(1))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            return ResponseEntity.ok().body(responseMessage);
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

    @PutMapping("/update_subscription")
    public ResponseEntity<String> updateSubscription(@RequestBody UpdateSubscriptionDTO request,
                                                     @RequestHeader("Authorization") String jwtToken,
                                                     HttpServletResponse response) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            User user = userRepository.findByPhoneNumber(userPhoneNumber);
            Subscription subscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(request.getNewSubscription().getSubscriptionName());
            if (subscription == null) {
                throw new NoSuchElementException("Subscription " + request.getNewSubscription().getSubscriptionName() + " not found");
            }
            TransactionDTO transactionDTO = TransactionDTO.builder()
                    .outputCardNumber(user.getUserBankCard().getCardNumber())
                    .sum(subscription.getSubscriptionPrice())
                    .cardExpirationDate(user.getUserBankCard().getCardExpirationDate())
                    .cvv(user.getUserBankCard().getCvv()).build();
            ResponseEntity<String> paymentResponse = paymentController.payment(transactionDTO);
            if (paymentResponse.getStatusCode().is2xxSuccessful()) {
                userService.updateSubscription(userPhoneNumber, request);
                log.info("Subscription for user with phone number {} updated successful", user.getPhoneNumber());
                String responseMessage = "Subscription " + subscription.getSubscriptionName() + " successful activated";
                UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(userPhoneNumber));
                String newJwtToken = jwtService.generateJwtToken(userDetails);
                ResponseCookie jwtCookie = ResponseCookie.from("JWT_TOKEN", newJwtToken)
                        .httpOnly(false)
                        .secure(false)
                        .path("/")
                        .maxAge(Duration.ofHours(1))
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
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
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e) {
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
            log.info("Auto renew status for user with phone number {} set successfully", userPhoneNumber);
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


    @GetMapping("/subscription")
    public ResponseEntity<?> findUsersCurrentSubscription(@RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            Subscription subscription = userService.findUsersCurrentSubscription(userPhoneNumber);
            return ResponseEntity.ok(subscription);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> findAllSubscriptions() {
        try {
            List<Subscription> subscriptions = subscriptionService.findAllSubscription();
            subscriptions.remove(subscriptionRepository.findBySubscriptionNameIgnoreCase("ADMIN"));
            subscriptions.remove(subscriptionRepository.findBySubscriptionNameIgnoreCase("REGISTRATION"));
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
    public ResponseEntity<?> findSubscriptionsByPrice(@RequestParam("minPrice") BigDecimal minPrice,
                                                      @RequestParam("maxPrice") BigDecimal maxPrice) {
        try {
            List<Subscription> subscriptions = subscriptionService.findSubscriptionsByPrice(minPrice, maxPrice);
            subscriptions.removeIf(subscription -> "ADMIN".equals(subscription.getSubscriptionName()));
            subscriptions.removeIf(subscription -> "REGISTRATION".equals(subscription.getSubscriptionName()));
            if (subscriptions.isEmpty()) {
                String responseMessage = "Subscriptions between price " + minPrice + " and " + maxPrice + " not found";
                System.out.println("Subscriptions between price " + minPrice + " and " + maxPrice + " not found");
                return ResponseEntity.ok(responseMessage);
            }
            System.out.println(subscriptions);
            log.info("Fetched subscription between price " + minPrice + " and " + maxPrice);
            return ResponseEntity.ok(subscriptions);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding subscription between price " + minPrice + " and " + maxPrice);
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

    @GetMapping("/subscription_end_time")
    public ResponseEntity<?> userSubscriptionExpiredTime(@RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            String formattedDateTime = userService.userSubscriptionExpiredTime(userPhoneNumber);
            return ResponseEntity.ok(formattedDateTime);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/auto_renew_status")
    public ResponseEntity<?> checkUsersAutoRenewStatus(@RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            String status = userService.checkUsersAutoRenewStatus(userPhoneNumber);
            return ResponseEntity.ok(status);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/phone_number")
    public ResponseEntity<?> getUserPhoneNumber(@RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            return ResponseEntity.ok(userPhoneNumber);
        } catch (Exception e) {
            log.error("Error finding user by subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/bank_card_number")
    public ResponseEntity<?> getUserBankCardNumber(@RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            long userBankCardNumber = Long.parseLong(String.valueOf(userService.getUserBankCard(userPhoneNumber).getCardNumber()));
            return ResponseEntity.ok(userBankCardNumber);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user by subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/email")
    public ResponseEntity<?> getUserEmail(@RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            String userEmail = userService.getUserEmail(userPhoneNumber);
            return ResponseEntity.ok(userEmail);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete_user_by_phone_number")
    public ResponseEntity<String> deleteUserByPhoneNumber(@RequestBody DeleteUserAccountDTO request,
                                                          @RequestHeader("Authorization") String jwtToken) {
        long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
        try {
            userService.deleteUserByPhoneNumber(userPhoneNumber, request.getPassword());
            log.info("User with phoneNumber {} delete successful", userPhoneNumber);
            String redirectUrl = "/host_page";
            URI location = URI.create(redirectUrl);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(location)
                    .build();
        }catch (IllegalArgumentException e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (NoSuchElementException e) {
            log.error("User with phone number {} not found {}", userPhoneNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting a user by phone number", e);
            String errorMessage = "Error deleting user with phone number " + userPhoneNumber;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
}