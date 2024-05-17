package ua.dymohlo.PetMusicStorage.controller;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ua.dymohlo.PetMusicStorage.dto.SubscriptionPaymentDTO;
import ua.dymohlo.PetMusicStorage.dto.TransactionDTO;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.repository.UserBankCardRepository;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payment")
public class PaymentController {
    private final UserBankCardRepository userBankCardRepository;
    private final WebClient.Builder webClientBuilder;
    private final SubscriptionRepository subscriptionRepository;


    //    @PostMapping
//    public ResponseEntity<String> subscriptionPayment(@RequestBody SubscriptionPaymentDTO request) {
//        try {
//            if (!userBankCardRepository.existsByCardNumber(request.getUserBankCard().getCardNumber())) {
//                log.error("Bank card with number {}  does not exists", request.getUserBankCard());
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bank card with number does not exists");
//            }
//            Subscription subscription = subscriptionRepository.findBySubscriptionName(request.getNewSubscription().getSubscriptionName());
//            if (!subscriptionRepository.existsBySubscriptionName(subscription.getSubscriptionName())) {
//                log.error("Subscription {} does not exists", request.getNewSubscription());
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Subscription does not exists");
//            }
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            long recipientBankCardNumber = 1234567890123456L;
//            BigDecimal paymentPrice = subscription.getSubscriptionPrice();
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
//            if (bankResponse.getStatusCode().is2xxSuccessful()) {
//                return ResponseEntity.ok("Payment success");
//            } else {
//                log.error("Payment failed for user with phone number {}", request.getUserPhoneNumber());
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment failed");
//            }
//        } catch (IllegalArgumentException e) {
//            log.error("Registration failed for user with phone number {}: {}", request.getUserPhoneNumber(), e.getMessage());
//            throw new IllegalStateException("An unexpected error occurred");
//        }
//    }
//    @PostMapping
//    public ResponseEntity<String> payment(@RequestBody TransactionDTO request) {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            long recipientBankCardNumber = 1234567890123456L;
//            BigDecimal paymentPrice = request.getSum();
//            String bankUrlTransaction = "http://localhost:8081/transaction";
//            TransactionDTO transactionDTO = TransactionDTO.builder()
//                    .outputCardNumber(request.getOutputCardNumber())
//                    .targetCardNumber(recipientBankCardNumber)
//                    .sum(paymentPrice)
//                    .cardExpirationDate(request.getCardExpirationDate())
//                    .cvv(request.getCvv()).build();
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
//            return bankResponse;
//        } catch (IllegalArgumentException e) {
//            log.error("An unexpected error occurred");
//            throw new IllegalStateException("An unexpected error occurred");
//        }
//    }
    @PostMapping
    public ResponseEntity<String> payment(@RequestBody TransactionDTO request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            long recipientBankCardNumber = 1234567890123456L;
            BigDecimal paymentPrice = request.getSum();
            String bankUrlTransaction = "http://localhost:8081/transaction";
            TransactionDTO transactionDTO = TransactionDTO.builder()
                    .outputCardNumber(request.getOutputCardNumber())
                    .targetCardNumber(recipientBankCardNumber)
                    .sum(paymentPrice)
                    .cardExpirationDate(request.getCardExpirationDate())
                    .cvv(request.getCvv()).build();

            Mono<ResponseEntity<String>> bankResponseMono = webClientBuilder.build()
                    .post()
                    .uri(bankUrlTransaction)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(transactionDTO)
                    .retrieve()
                    .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new IllegalArgumentException("Payment failed: " + errorBody))))
                    .toEntity(String.class);

            ResponseEntity<String> bankResponse = bankResponseMono.block();
            return bankResponse;
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred", e);
            throw new IllegalStateException("An unexpected error occurred", e);
        }
    }

}