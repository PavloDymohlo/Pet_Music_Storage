package ua.dymohlo.PetMusicStorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMusicFileNameDTO {
    private String currentMusicFileName;
    private String newMusicFileName;
}
