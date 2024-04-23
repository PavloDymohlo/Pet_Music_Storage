package ua.dymohlo.PetMusicStorage.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ua.dymohlo.PetMusicStorage.service.JWTService;

import static org.mockito.Mockito.when;

public class JWTServiceTest {
    private JWTService jwtService;
    private String mockJwtSigningKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JWTService();
    }

    @Test
    public void JWTServiceCConstructor_jwtSigningKeyIsNotNull() {
        //mockJwtSigningKey = System.getenv("SECRET_KEY");
        when(System.getenv("SECRET_KEY")).thenReturn("mockSecretKeyValue");
        if (mockJwtSigningKey == null) {
            throw new IllegalStateException("SECRET_KEY environment variable not found!");
        }

        new JWTService();
    }
}
