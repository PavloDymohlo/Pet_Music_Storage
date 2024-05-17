package ua.dymohlo.PetMusicStorage.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ua.dymohlo.PetMusicStorage.dto.TransactionDTO;
import ua.dymohlo.PetMusicStorage.dto.UserRegistrationDTO;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;
import org.springframework.web.reactive.function.client.WebClient;

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

    //    @PostMapping
//    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDTO request) {
//        try {
//            if (userService.isPhoneNumberRegistered(request.getPhoneNumber())) {
//                log.error("User with phone number {} already exists", request.getPhoneNumber());
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with phone number already exists");
//            }
//            if (userService.isEmailRegistered(request.getEmail())) {
//                log.error("User with email {} already exists", request.getEmail());
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with email already exists");
//            }
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            long recipientBankCardNumber = 1234567890123456L;
//            BigDecimal paymentPrice = BigDecimal.valueOf(1);
//            String bankUrlTransaction = "http://localhost:8081/transaction";
//            TransactionDTO transactionDTO = TransactionDTO.builder()
//                    .outputCardNumber(request.getUserBankCard().getCardNumber())
//                    .targetCardNumber(recipientBankCardNumber)
//                    .sum(paymentPrice)
//                    .cardExpirationDate(request.getUserBankCard().getCardExpirationDate())
//                    .cvv(request.getUserBankCard().getCvv()).build();
//
//            Mono<ResponseEntity<String>> bankResponseMono = webClientBuilder.build()
//                    .post()
//                    .uri(bankUrlTransaction)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(transactionDTO)
//                    .retrieve()
//                    .toEntity(String.class);
//
//            ResponseEntity<String> bankResponse = bankResponseMono.block();
//
//            if (bankResponse.getStatusCode().is2xxSuccessful()) {
//                userService.registerUser(request);
//                UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getPhoneNumber()));
//                String jwtToken = jwtService.generateJwtToken(userDetails);
//                log.info("User registered successfully with phone number {}", request.getPhoneNumber());
//                log.info("Generated JWT token: {}", jwtToken);
//                return ResponseEntity.ok(jwtToken);
//            } else {
//                log.error("Payment failed for user with phone number {}", request.getPhoneNumber());
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment failed");
//            }
//        } catch (IllegalArgumentException e) {
//            log.error("Registration failed for user with phone number {}: {}", request.getPhoneNumber(), e.getMessage());
//            throw new IllegalStateException("An unexpected error occurred");
//        }
//    }
    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDTO request) {
        try {
            if (userService.isPhoneNumberRegistered(request.getPhoneNumber())) {
                log.error("User with phone number {} already exists", request.getPhoneNumber());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with phone number already exists");
            }
            if (userService.isEmailRegistered(request.getEmail())) {
                log.error("User with email {} already exists", request.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with email already exists");
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
                log.info("User registered successfully with phone number {}", request.getPhoneNumber());
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