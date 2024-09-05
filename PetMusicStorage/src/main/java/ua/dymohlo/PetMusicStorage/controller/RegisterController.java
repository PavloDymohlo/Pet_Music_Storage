package ua.dymohlo.PetMusicStorage.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.PetMusicStorage.dto.TransactionDTO;
import ua.dymohlo.PetMusicStorage.dto.UserRegistrationDTO;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.EmailService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/register")
public class RegisterController {
    private final UserService userService;
    private final DatabaseUserDetailsService databaseUserDetailsService;
    private final JWTService jwtService;
    private final PaymentController paymentController;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO request, HttpServletResponse response) {
        try {
            if (userService.isPhoneNumberRegistered(request.getPhoneNumber())) {
                log.error("User with phone number {} already exists", request.getPhoneNumber());
                String errorMessage = "User with phone number " + request.getPhoneNumber() + " already exists";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
            if (userService.isEmailRegistered(request.getEmail())) {
                log.error("User with email {} already exists", request.getEmail());
                String errorMessage = "User with email " + request.getEmail() + " already exists";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
            BigDecimal bonusPrice = subscriptionRepository.findBySubscriptionNameIgnoreCase("REGISTRATION").getSubscriptionPrice();
            TransactionDTO transactionDTO = TransactionDTO.builder()
                    .outputCardNumber(request.getUserBankCard().getCardNumber())
                    .sum(bonusPrice)
                    .cardExpirationDate(request.getUserBankCard().getCardExpirationDate())
                    .cvv(request.getUserBankCard().getCvv()).build();
            ResponseEntity<String> paymentResponse = paymentController.payment(transactionDTO);
            if (paymentResponse.getStatusCode().is2xxSuccessful()) {
                User user = userService.registerUser(request);
                UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getPhoneNumber()));
                String jwtToken = jwtService.generateJwtToken(userDetails);
                log.info("User with phone number {} registered successfully ", request.getPhoneNumber());
                log.info("Generated JWT token: {}", jwtToken);
                String telegramBotLink = "https://t.me/musicStorageMessage_bot?start=" + user.getPhoneNumber();
                log.info("Telegram bot link generated for user: {}", telegramBotLink);
                ResponseCookie jwtCookie = ResponseCookie.from("JWT_TOKEN", jwtToken)
                        .httpOnly(false)
                        .secure(false)
                        .path("/")
                        .maxAge(Duration.ofHours(1))
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
                String redirectUrl = "/personal_office";
                URI location = URI.create(redirectUrl);
                String emailText = "You register successful!";
                try {
                    emailService.sendEmail(request.getEmail(), emailText);
                } catch (Exception e) {
                    log.error("Failed to send registration email", e);
                }
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(location)
                        .header("Telegram-Bot-Link", telegramBotLink)
                        .build();
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