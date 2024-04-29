package ua.dymohlo.PetMusicStorage.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.reactive.function.client.WebClient;
import ua.dymohlo.PetMusicStorage.service.JWTService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
public class WebClientConfigTest {
    @Autowired
    private WebClient.Builder webClientBuilder;
    @MockBean
    private JWTService mockJwtService;
    @MockBean
    private UserDetails mockUserDetails;

    @Test
    public void webClientBuilder_beanExists() {
        when(mockJwtService.generateJwtToken(mockUserDetails)).thenReturn("mocked_secret_key");
        assertNotNull(webClientBuilder);
    }
}
