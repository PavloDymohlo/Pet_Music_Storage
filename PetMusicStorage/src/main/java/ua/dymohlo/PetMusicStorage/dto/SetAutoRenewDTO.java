package ua.dymohlo.PetMusicStorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.dymohlo.PetMusicStorage.Enum.AutoRenewStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetAutoRenewDTO {
    private long userPhoneNumber;
    private AutoRenewStatus autoRenewStatus;
}
