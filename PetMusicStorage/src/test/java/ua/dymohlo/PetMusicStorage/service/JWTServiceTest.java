package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @InjectMocks
    private JWTService jwtService;

    @Mock
    private DatabaseUserDetailsService userDetailsService;
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

    @Test
    void extractUserName_success() {
        UserDetails userDetails = new User("809944588901", "password", Collections.singleton(new SimpleGrantedAuthority("FREE")));

        String token = jwtService.generateJwtToken(userDetails);
        String extractedUsername = jwtService.extractUserName(token);

        assertEquals("809944588901", extractedUsername);
    }

    @Test
    void testGetRoles() {
        UserDetails userDetails = new User("1234567890", "password", Collections.singleton(new SimpleGrantedAuthority("PREMIUM")));

        String token = jwtService.generateJwtToken(userDetails);
        List<String> roles = jwtService.getRoles(token);

        assertEquals(1, roles.size());
        assertTrue(roles.contains("ROLE_PREMIUM"));
    }

    @Test
    void testValidateToken_ValidToken() {
        UserDetails userDetails = new User("1234567890", "password", Collections.singleton(new SimpleGrantedAuthority("FREE")));

        String token = jwtService.generateJwtToken(userDetails);

        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void testValidateToken_ExpiredToken() throws InterruptedException {
        UserDetails userDetails = new User("1234567890", "password", Collections.singleton(new SimpleGrantedAuthority("FREE")));

        ReflectionTestUtils.setField(jwtService, "jwtExpirationMinutes", 0);
        String token = jwtService.generateJwtToken(userDetails);

        Thread.sleep(1000); // Wait for 1 second to ensure token expiration

        assertFalse(jwtService.validateToken(token, userDetails));
    }

    @Test
    void testValidateToken_InvalidUsername() {
        UserDetails userDetails = new User("1234567890", "password", Collections.singleton(new SimpleGrantedAuthority("FREE")));

        String token = jwtService.generateJwtToken(userDetails);

        UserDetails invalidUser = new User("9876543210", "password", Collections.singleton(new SimpleGrantedAuthority("FREE")));

        assertFalse(jwtService.validateToken(token, invalidUser));
    }

    @Test
    void testConstructor_MissingEnvironmentVariable() {
        assertThrows(IllegalStateException.class, () -> {
            new JWTService();
        });
    }
}