package ua.dymohlo.PetMusicStorage.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ua.dymohlo.PetMusicStorage.dto.TransactionDTO;
import ua.dymohlo.PetMusicStorage.dto.UserRegistrationDTO;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/register")
public class RegisterController {
    private final UserService userService;
    private final DatabaseUserDetailsService databaseUserDetailsService;
    private final JWTService jwtService;

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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            TransactionDTO transactionDTO = TransactionDTO.builder()
                    .outputCardNumber(request.getUserBankCard().getCardNumber())
                    .targetCardNumber(1234567890123456L)
                    .sum(100)
                    .cardExpirationDate(request.getUserBankCard().getCardExpirationDate())
                    .cvv(request.getUserBankCard().getCvv()).build();
            HttpEntity<TransactionDTO> cardRequest = new HttpEntity<>(transactionDTO, headers);
            ResponseEntity<String> bankResponse;
            try {
                bankResponse = new RestTemplate().exchange("http://localhost:8081/transaction",
                        HttpMethod.POST, cardRequest, String.class);
            } catch (HttpClientErrorException | HttpServerErrorException ex) {
                return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
            }
            if (bankResponse.getStatusCode().is2xxSuccessful()) {
                userService.registerUser(request);
                UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getPhoneNumber()));
                String jwtToken = jwtService.generateJwtToken(userDetails);
                log.info("User registered successfully with phone number {}", request.getPhoneNumber());
                log.info("Generated JWT token: {}", jwtToken);
                return ResponseEntity.ok(jwtToken);
            } else {
                log.error("Payment failed for user with phone number {}", request.getPhoneNumber());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment failed");
            }
        } catch (IllegalArgumentException e) {
            log.error("Registration failed for user with phone number {}: {}", request.getPhoneNumber(), e.getMessage());
            throw new IllegalStateException("An unexpected error occurred");
        }
    }
}