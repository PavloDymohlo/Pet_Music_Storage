package ua.dymohlo.PetMusicStorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateSubscriptionDurationTimeDTO {
    private String subscriptionName;
    private int newDurationTime;
}