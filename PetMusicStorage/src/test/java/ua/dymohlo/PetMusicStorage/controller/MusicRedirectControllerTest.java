package ua.dymohlo.PetMusicStorage.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ua.dymohlo.PetMusicStorage.configuration.TestSecurityConfig;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MusicRedirectController.class)
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
public class MusicRedirectControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserService userService;
    @MockBean
    private User user;
    @MockBean
    private JWTService jwtService;

    @Test
    public void getMusicPage_success() throws Exception {
        long phoneNumber = 911L;
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        user = User.builder()
                .subscription(subscription).build();
        String jwtToken = "mock-jwt-token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(phoneNumber);
        when(userService.findUserByPhoneNumber(phoneNumber)).thenReturn(user);

        mvc.perform(get("/music/get_music_page")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }
}
