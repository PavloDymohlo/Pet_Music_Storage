package ua.dymohlo.PetMusicStorage.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.dymohlo.PetMusicStorage.service.JWTService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

//@SpringBootTest
//public class PasswordEncoderConfigTest {
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//    @MockBean
//    private JWTService mockJwtService;
//    @MockBean
//    private UserDetails mockUserDetails;
//
//    @Test
//    public void passwordEncoder_beanExists() {
//        when(mockJwtService.generateJwtToken(mockUserDetails)).thenReturn("mocked_secret_key");
//        assertNotNull(passwordEncoder);
//    }
//}
