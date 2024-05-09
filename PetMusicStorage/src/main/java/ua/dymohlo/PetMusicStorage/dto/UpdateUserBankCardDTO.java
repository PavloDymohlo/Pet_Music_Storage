package ua.dymohlo.PetMusicStorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserBankCardDTO {
    private long userPhoneNumber;
    private UserBankCard newUserBankCard;
}