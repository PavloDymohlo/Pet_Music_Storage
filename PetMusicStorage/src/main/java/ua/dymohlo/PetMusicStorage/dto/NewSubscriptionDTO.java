package ua.dymohlo.PetMusicStorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewSubscriptionDTO {
    private String subscriptionName;
    private BigDecimal subscriptionPrice;
    private int subscriptionDurationTime;
}
