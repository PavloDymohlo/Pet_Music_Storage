package ua.dymohlo.MockBankSystem.dto;

import lombok.Data;

@Data
public class TransactionDTO {
    private long outputCardNumber;
    private long targetCardNumber;
    private int sum;
    private String cardExpirationDate;
    private short cvv;
}
