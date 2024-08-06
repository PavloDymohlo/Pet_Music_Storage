package ua.dymohlo.MockBankSystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.MockBankSystem.dto.TransactionDTO;
import ua.dymohlo.MockBankSystem.service.ClientService;

@RestController
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping("/transaction")
    public ResponseEntity<String> transactionMoney(@RequestBody TransactionDTO request) {
        long outputCardNumber = request.getOutputCardNumber();
        long targetCardNumber = request.getTargetCardNumber();
        int sum = request.getSum();
        String cardExpirationDate = request.getCardExpirationDate();
        short cvv = request.getCvv();
        try {
            clientService.transactionMoney(outputCardNumber, targetCardNumber, sum, cardExpirationDate, cvv);
            return ResponseEntity.ok("Transaction successful!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Transaction failed: " + e.getMessage());
        }
    }
}