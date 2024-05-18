package ua.dymohlo.PetMusicStorage.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.PetMusicStorage.dto.TransactionDTO;
import ua.dymohlo.PetMusicStorage.dto.UserRegistrationDTO;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/register")
public class RegisterController {
    private final UserService userService;
    private final DatabaseUserDetailsService databaseUserDetailsService;
    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final PaymentController paymentController;

    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDTO request) {
        try {
            if (userService.isPhoneNumberRegistered(request.getPhoneNumber())) {
                log.error("User with phone number {} already exists", request.getPhoneNumber());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with phone number " + request.getPhoneNumber() + " already exists");
            }
            if (userService.isEmailRegistered(request.getEmail())) {
                log.error("User with email {} already exists", request.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with email " + request.getEmail() + " already exists");
            }

            BigDecimal bonusPrice = BigDecimal.valueOf(1);
            TransactionDTO transactionDTO = TransactionDTO.builder()
                    .outputCardNumber(request.getUserBankCard().getCardNumber())
                    .sum(bonusPrice)
                    .cardExpirationDate(request.getUserBankCard().getCardExpirationDate())
                    .cvv(request.getUserBankCard().getCvv()).build();
            ResponseEntity<String> paymentResponse = paymentController.payment(transactionDTO);
            if (paymentResponse.getStatusCode().is2xxSuccessful()) {
                userService.registerUser(request);
                UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getPhoneNumber()));
                String jwtToken = jwtService.generateJwtToken(userDetails);
                log.info("User with phone number {} registered successfully ", request.getPhoneNumber());
                log.info("Generated JWT token: {}", jwtToken);
                return ResponseEntity.ok(jwtToken);
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