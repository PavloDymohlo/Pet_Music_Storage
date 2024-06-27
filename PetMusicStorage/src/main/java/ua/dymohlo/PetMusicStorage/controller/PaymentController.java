package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ua.dymohlo.PetMusicStorage.dto.TransactionDTO;
import ua.dymohlo.PetMusicStorage.entity.BankTransactionData;
import ua.dymohlo.PetMusicStorage.repository.PaymentRecipientDataRepository;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payment")
public class PaymentController {
    private final WebClient.Builder webClientBuilder;
    private final PaymentRecipientDataRepository paymentRecipientDataRepository;

    @PostMapping
    public ResponseEntity<String> payment(@RequestBody TransactionDTO request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String currentTransactionData = "current_transaction";
            long recipientBankCardNumber = paymentRecipientDataRepository.findByTransactionalName(currentTransactionData)
                    .getRecipientBankCard().getBankCardNumber();
            BigDecimal paymentPrice = request.getSum();
            String bankUrlTransaction = paymentRecipientDataRepository.findByTransactionalName(currentTransactionData)
                    .getBankTransactionData().getBankUrlTransaction();
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