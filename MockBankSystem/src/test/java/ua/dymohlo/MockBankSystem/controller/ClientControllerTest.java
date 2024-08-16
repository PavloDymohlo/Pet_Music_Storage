package ua.dymohlo.MockBankSystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.dymohlo.MockBankSystem.dto.TransactionDTO;
import ua.dymohlo.MockBankSystem.service.ClientService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
public class ClientControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ClientService clientService;
    private TransactionDTO transactionDTO;

    @BeforeEach
    void setUp() {
        transactionDTO = new TransactionDTO();
        transactionDTO.setOutputCardNumber(1234567890123456L);
        transactionDTO.setTargetCardNumber(6543210987654321L);
        transactionDTO.setSum(100);
        transactionDTO.setCardExpirationDate("12/25");
        transactionDTO.setCvv((short) 123);
    }

    @Test
    void transactionMoney_response_ok() throws Exception {
        Mockito.doNothing().when(clientService).transactionMoney(
                Mockito.eq(transactionDTO.getOutputCardNumber()),
                Mockito.eq(transactionDTO.getTargetCardNumber()),
                Mockito.eq(transactionDTO.getSum()),
                Mockito.eq(transactionDTO.getCardExpirationDate()),
                Mockito.eq(transactionDTO.getCvv())
        );

        mockMvc.perform(post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(transactionDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction successful!"));
    }


    @Test
    void transactionMoney_response_bad_request() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Invalid data")).when(clientService).transactionMoney(
                Mockito.eq(transactionDTO.getOutputCardNumber()),
                Mockito.eq(transactionDTO.getTargetCardNumber()),
                Mockito.eq(transactionDTO.getSum()),
                Mockito.eq(transactionDTO.getCardExpirationDate()),
                Mockito.eq(transactionDTO.getCvv())
        );

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(transactionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Transaction failed: Invalid data"));
    }

}
