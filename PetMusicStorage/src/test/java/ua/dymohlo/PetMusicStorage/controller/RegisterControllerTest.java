package ua.dymohlo.PetMusicStorage.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ua.dymohlo.PetMusicStorage.dto.UserRegistrationDTO;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = RegisterController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService mockUserService;

    @MockBean
    private DatabaseUserDetailsService mockDatabaseUserDetailsService;

    @MockBean
    private JWTService mockJwtService;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @Test
    public void registerUser_successfulRegistration() throws Exception {
        WebClient.RequestBodyUriSpec requestBodyUriSpecMock = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpecMock = Mockito.mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(Mockito.mock(WebClient.class));
        when(webClientBuilder.build().post()).thenReturn(requestBodyUriSpecMock);
        when(webClientBuilder.build().post().uri(anyString())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any())).thenReturn(requestHeadersSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve()).thenReturn(responseSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve().toEntity(String.class)).thenReturn(Mono.just(ResponseEntity.ok().body("mockResponse")));
        when(mockUserService.isPhoneNumberRegistered(anyLong())).thenReturn(false);
        when(mockUserService.isEmailRegistered(anyString())).thenReturn(false);
        when(mockJwtService.generateJwtToken(any(UserDetails.class))).thenReturn("mockJwtToken");

        UserBankCard mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("25/25")
                .cvv((short) 111).build();
        UserRegistrationDTO mockUserRegistrationDTO = UserRegistrationDTO.builder()
                .phoneNumber(80988887520L)
                .userBankCard(mockUserBankCard)
                .email("useremail@email.com")
                .password("password").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockUserRegistrationDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(mockUserService, times(1)).registerUser(mockUserRegistrationDTO);
    }

    @Test
    public void registerUser_responseBadRequest() throws Exception {
        WebClient.RequestBodyUriSpec requestBodyUriSpecMock = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpecMock = Mockito.mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(Mockito.mock(WebClient.class));
        when(webClientBuilder.build().post()).thenReturn(requestBodyUriSpecMock);
        when(webClientBuilder.build().post().uri(anyString())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any())).thenReturn(requestHeadersSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve()).thenReturn(responseSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve().toEntity(String.class))
                .thenReturn(Mono.just(ResponseEntity.badRequest().body("mockResponse")));

        when(mockUserService.isPhoneNumberRegistered(anyLong())).thenReturn(false);
        when(mockUserService.isEmailRegistered(anyString())).thenReturn(false);
        when(mockJwtService.generateJwtToken(any(UserDetails.class))).thenReturn("mockJwtToken");

        UserBankCard mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("25/25")
                .cvv((short) 111).build();
        UserRegistrationDTO mockUserRegistrationDTO = UserRegistrationDTO.builder()
                .phoneNumber(80988887520L)
                .userBankCard(mockUserBankCard)
                .email("useremail@email.com")
                .password("password").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockUserRegistrationDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()); // Очікуваний статус "400 Bad Request"

        verify(mockUserService, never()).registerUser(any(UserRegistrationDTO.class));
        verify(mockUserService, times(1)).isPhoneNumberRegistered(mockUserRegistrationDTO.getPhoneNumber());
        verify(mockUserService, times(1)).isEmailRegistered(mockUserRegistrationDTO.getEmail());
    }

    @Test
    public void registerUser_phoneNumberExists() throws Exception {
        UserBankCard mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("25/25")
                .cvv((short) 111).build();
        UserRegistrationDTO mockUserRegistrationDTO = UserRegistrationDTO.builder()
                .phoneNumber(80988887520L)
                .userBankCard(mockUserBankCard)
                .email("useremail@email.com")
                .password("password").build();

        when(mockUserService.isPhoneNumberRegistered(anyLong())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockUserRegistrationDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("User with phone number already exists"));
    }

    @Test
    public void registerUser_emailExists() throws Exception {
        UserBankCard mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("25/25")
                .cvv((short) 111).build();
        UserRegistrationDTO mockUserRegistrationDTO = UserRegistrationDTO.builder()
                .phoneNumber(80988887520L)
                .userBankCard(mockUserBankCard)
                .email("useremail@email.com")
                .password("password").build();

        when(mockUserService.isEmailRegistered(anyString())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockUserRegistrationDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("User with email already exists"));
    }
}