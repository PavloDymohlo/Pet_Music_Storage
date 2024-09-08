package ua.dymohlo.MockBankSystem.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.MockBankSystem.dto.TransactionDTO;
import ua.dymohlo.MockBankSystem.entity.Client;
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

    @PostMapping("/add")
    public ResponseEntity<String> addClient(@RequestBody Client client) {
        try {
            clientService.addClient(client);
            return ResponseEntity.ok("Client added successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid client data: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}