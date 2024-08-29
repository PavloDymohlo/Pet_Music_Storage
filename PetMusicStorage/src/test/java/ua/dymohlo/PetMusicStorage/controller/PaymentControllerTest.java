package ua.dymohlo.PetMusicStorage.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ua.dymohlo.PetMusicStorage.configuration.TestSecurityConfig;
import ua.dymohlo.PetMusicStorage.dto.TransactionDTO;
import ua.dymohlo.PetMusicStorage.repository.PaymentRecipientDataRepository;
import ua.dymohlo.PetMusicStorage.service.JWTService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(TestSecurityConfig.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PaymentRecipientDataRepository paymentRecipientDataRepository;
    @MockBean
    private WebClient.Builder webClientBuilder;
    @MockBean
    private JWTService jwtService;
    @MockBean
    private PaymentController paymentController;

    @Test
    void payment_success() throws Exception {
        TransactionDTO request = TransactionDTO.builder()
                .outputCardNumber(1234567890123456L)
                .cardExpirationDate("12/25")
                .cvv((short) 123)
                .sum(new BigDecimal("100.00")).build();
        WebClient.RequestBodyUriSpec requestBodyUriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        String requestJson = objectMapper.writeValueAsString(request);

        when(webClientBuilder.build()).thenReturn(mock(WebClient.class));
        when(webClientBuilder.build().post()).thenReturn(requestBodyUriSpecMock);
        when(webClientBuilder.build().post().uri(anyString())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any())).thenReturn(requestHeadersSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve()).thenReturn(responseSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve().toEntity(String.class)).thenReturn(Mono.just(ResponseEntity.ok().body("mockResponse")));

        when(paymentController.payment(any(TransactionDTO.class)))
                .thenReturn(ResponseEntity.ok("success"));

        mockMvc.perform(post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    @Test
    void payment_paymentFailed() throws Exception {
        TransactionDTO request = TransactionDTO.builder()
                .outputCardNumber(1234567890123456L)
                .cardExpirationDate("12/25")
                .cvv((short) 123)
                .sum(new BigDecimal("100.00")).build();
        WebClient.RequestBodyUriSpec requestBodyUriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        String requestJson = objectMapper.writeValueAsString(request);

        when(webClientBuilder.build()).thenReturn(mock(WebClient.class));
        when(webClientBuilder.build().post()).thenReturn(requestBodyUriSpecMock);
        when(webClientBuilder.build().post().uri(anyString())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any())).thenReturn(requestBodySpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any())).thenReturn(requestHeadersSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve()).thenReturn(responseSpecMock);
        when(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any()).retrieve().toEntity(String.class)).thenReturn(Mono.just(ResponseEntity.ok().body("mockResponse")));

        when(paymentController.payment(any(TransactionDTO.class)))
                .thenReturn(ResponseEntity.badRequest().body("Payment failed"));

        mockMvc.perform(post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment failed"));
    }
}