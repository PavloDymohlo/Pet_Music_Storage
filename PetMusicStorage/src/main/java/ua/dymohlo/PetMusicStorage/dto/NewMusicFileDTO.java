package ua.dymohlo.PetMusicStorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.dymohlo.PetMusicStorage.entity.Subscription;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewMusicFileDTO {
    private String musicFileName;
    private String filePath;
    private Subscription subscription;
}
