package ua.dymohlo.PetMusicStorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import ua.dymohlo.PetMusicStorage.configuration.TestSecurityConfig;
import ua.dymohlo.PetMusicStorage.dto.UserLoginInDTO;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginInController.class)
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
public class LoginControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserService userService;
    @MockBean
    private JWTService jwtService;
    @MockBean
    private DatabaseUserDetailsService databaseUserDetailsService;
    @MockBean
    private UserDetails userDetails;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void loginIn_redirectToAdminOffice_success() throws Exception {
        UserLoginInDTO request = UserLoginInDTO.builder()
                .phoneNumber(911L).build();
        String jwtToken = "test-jwt-token";

        when(userService.loginIn(request)).thenReturn("success");
        when(databaseUserDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateJwtToken(userDetails)).thenReturn(jwtToken);
        when(userService.isAdminSubscription(request.getPhoneNumber())).thenReturn(true);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "/admin_office"));
    }

    @Test
    public void loginIn_redirectToPersonalOffice_success() throws Exception {
        UserLoginInDTO request = UserLoginInDTO.builder()
                .phoneNumber(911L).build();
        String jwtToken = "test-jwt-token";

        when(userService.loginIn(request)).thenReturn("success");
        when(databaseUserDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateJwtToken(userDetails)).thenReturn(jwtToken);
        when(userService.isAdminSubscription(request.getPhoneNumber())).thenReturn(false);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "/personal_office"));
    }
}