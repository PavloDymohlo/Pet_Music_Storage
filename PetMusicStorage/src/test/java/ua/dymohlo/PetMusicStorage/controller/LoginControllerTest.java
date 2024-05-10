package ua.dymohlo.PetMusicStorage.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import ua.dymohlo.PetMusicStorage.dto.UserLoginInDTO;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import static org.mockito.Mockito.when;


@WebMvcTest(controllers = LoginInController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService mockUserService;
    @MockBean
    private JWTService mockJwtService;
    @MockBean
    private DatabaseUserDetailsService mockDatabaseUserDetailsService;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void loginIn_redirectedToAdminOfficePage() throws Exception {
        UserLoginInDTO userLoginInDTO = UserLoginInDTO.builder()
                .phoneNumber(80981213335L)
                .password("password").build();
        Subscription subscription = Subscription.builder().subscriptionName("ADMIN").build(); // Змінено на "ADMIN"
        User mockUser = User.builder()
                .phoneNumber(userLoginInDTO.getPhoneNumber())
                .password("encodedPassword")
                .subscription(subscription)
                .build();
        when(mockUserService.isAdminSubscription(userLoginInDTO.getPhoneNumber())).thenReturn(true);
        when(mockUserService.loginIn(userLoginInDTO)).thenReturn("Success");
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                String.valueOf(mockUser.getPhoneNumber()),
                mockUser.getPassword(),
                Collections.singleton(() -> "ROLE_" + mockUser.getSubscription().getSubscriptionName())
        );
        when(mockDatabaseUserDetailsService.loadUserByUsername(String.valueOf(userLoginInDTO.getPhoneNumber())))
                .thenReturn(userDetails);
        String jwtToken = "mockedJWTToken";
        when(mockJwtService.generateJwtToken(userDetails)).thenReturn(jwtToken);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":80981213335,\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.redirectUrl").value("/admin_office"))
                .andExpect(jsonPath("$.jwtToken").value(jwtToken));
    }

    @Test
    public void loginIn_redirectedToPersonalOfficePage() throws Exception {
        UserLoginInDTO userLoginInDTO = UserLoginInDTO.builder()
                .phoneNumber(80981213335L)
                .password("password").build();
        Subscription subscription = Subscription.builder().subscriptionName("ADMIN").build(); // Змінено на "ADMIN"
        User mockUser = User.builder()
                .phoneNumber(userLoginInDTO.getPhoneNumber())
                .password("encodedPassword")
                .subscription(subscription)
                .build();
        when(mockUserService.isAdminSubscription(userLoginInDTO.getPhoneNumber())).thenReturn(false);
        when(mockUserService.loginIn(userLoginInDTO)).thenReturn("Success");
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                String.valueOf(mockUser.getPhoneNumber()),
                mockUser.getPassword(),
                Collections.singleton(() -> "ROLE_" + mockUser.getSubscription().getSubscriptionName())
        );
        when(mockDatabaseUserDetailsService.loadUserByUsername(String.valueOf(userLoginInDTO.getPhoneNumber())))
                .thenReturn(userDetails);
        String jwtToken = "mockedJWTToken";
        when(mockJwtService.generateJwtToken(userDetails)).thenReturn(jwtToken);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":80981213335,\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.redirectUrl").value("/personal_office"))
                .andExpect(jsonPath("$.jwtToken").value(jwtToken));
    }

}
