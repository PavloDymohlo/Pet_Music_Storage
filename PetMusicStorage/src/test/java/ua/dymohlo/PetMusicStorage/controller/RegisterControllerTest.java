package ua.dymohlo.PetMusicStorage.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ua.dymohlo.PetMusicStorage.PetMusicStorageApplication;
import ua.dymohlo.PetMusicStorage.dto.TransactionDTO;
import ua.dymohlo.PetMusicStorage.dto.UserRegistrationDTO;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.EmailService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PetMusicStorageApplication.class)
@AutoConfigureMockMvc
public class RegisterControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    @MockBean
    private DatabaseUserDetailsService databaseUserDetailsService;
    @MockBean
    private JWTService jwtService;
    @MockBean
    private WebClient.Builder webClientBuilder;
    @MockBean
    private EmailService emailService;
    @MockBean
    private PaymentController paymentController;

    @Test
    public void registerUser_success() throws Exception {
        User user = User.builder()
                .phoneNumber(80663322110L).build();
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("25/25")
                .cvv((short) 111).build();
        UserRegistrationDTO request = UserRegistrationDTO.builder()
                .phoneNumber(80988887520L)
                .userBankCard(userBankCard)
                .email("useremail@email.com")
                .password("password").build();
        String jwtToken = "mockJwtToken";
        String requestJson = objectMapper.writeValueAsString(request);
        WebClient.RequestBodyUriSpec requestBodyUriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(mock(WebClient.class));
        when(webClientBuilder.build().post()).thenReturn(requestBodyUriSpecMock);
        when(webClientBuilder.build().post().uri(anyString())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()))
                .thenReturn(requestHeadersSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve())
                .thenReturn(responseSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve()
                .toEntity(String.class)).thenReturn(Mono.just(ResponseEntity.ok().body("mockResponse")));
        when(userService.isPhoneNumberRegistered(anyLong())).thenReturn(false);
        when(userService.isEmailRegistered(anyString())).thenReturn(false);
        when(jwtService.generateJwtToken(any(UserDetails.class))).thenReturn(jwtToken);
        when(paymentController.payment(any(TransactionDTO.class)))
                .thenReturn(ResponseEntity.ok("success"));
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(user);

        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isFound());
    }

    @Test
    public void registerUser_paymentFailed() throws Exception {
        UserBankCard mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("25/25")
                .cvv((short) 111).build();
        UserRegistrationDTO request = UserRegistrationDTO.builder()
                .phoneNumber(80988887520L)
                .userBankCard(mockUserBankCard)
                .email("useremail@email.com")
                .password("password").build();
        String jwtToken = "mockJwtToken";
        String requestJson = objectMapper.writeValueAsString(request);
        WebClient.RequestBodyUriSpec requestBodyUriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(mock(WebClient.class));
        when(webClientBuilder.build().post()).thenReturn(requestBodyUriSpecMock);
        when(webClientBuilder.build().post().uri(anyString())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()))
                .thenReturn(requestHeadersSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve())
                .thenReturn(responseSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve()
                .toEntity(String.class))
                .thenReturn(Mono.just(ResponseEntity.badRequest().body("mockResponse")));
        when(userService.isPhoneNumberRegistered(anyLong())).thenReturn(false);
        when(userService.isEmailRegistered(anyString())).thenReturn(false);
        when(jwtService.generateJwtToken(any(UserDetails.class))).thenReturn(jwtToken);
        when(paymentController.payment(any(TransactionDTO.class)))
                .thenReturn(ResponseEntity.badRequest().body("Payment failed"));

        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment failed"));
    }

    @Test
    public void registerUser_phoneNumberExists() throws Exception {
        UserBankCard mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("25/25")
                .cvv((short) 111).build();
        UserRegistrationDTO request = UserRegistrationDTO.builder()
                .phoneNumber(80988887520L)
                .userBankCard(mockUserBankCard)
                .email("useremail@email.com")
                .password("password").build();
        String requestJson = objectMapper.writeValueAsString(request);

        when(userService.isPhoneNumberRegistered(anyLong())).thenReturn(true);

        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User with phone number "
                        + request.getPhoneNumber() + " already exists"));
    }

    @Test
    public void registerUser_emailExists() throws Exception {
        UserBankCard mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("25/25")
                .cvv((short) 111).build();
        UserRegistrationDTO request = UserRegistrationDTO.builder()
                .phoneNumber(80988887520L)
                .userBankCard(mockUserBankCard)
                .email("useremail@email.com")
                .password("password").build();
        String requestJson = objectMapper.writeValueAsString(request);

        when(userService.isEmailRegistered(anyString())).thenReturn(true);

        mvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User with email " + request.getEmail() + " already exists"));
    }
}