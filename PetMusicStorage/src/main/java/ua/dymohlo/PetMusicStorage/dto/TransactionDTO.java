package ua.dymohlo.PetMusicStorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDTO {
    private long outputCardNumber;
    private long targetCardNumber;
    private int sum;
    private String cardExpirationDate;
    private short cvv;
}