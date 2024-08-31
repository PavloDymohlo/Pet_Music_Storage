package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @InjectMocks
    private JWTService jwtService;
    private static final String MOCK_SECRET_KEY = "mock_secret_key";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JWTService(MOCK_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtSigningKey", "testSecretKey");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMinutes", 60);
    }

    @Test
    void generateJwtToken_success() {
        UserDetails userDetails = new User("809944588901", "password", Collections.singleton(new SimpleGrantedAuthority("FREE")));

        String token = jwtService.generateJwtToken(userDetails);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }
}