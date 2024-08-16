package ua.dymohlo.MockBankSystem.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.MockBankSystem.entity.Client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
public class ClientRepositoryTest {
    @Mock
    private Client client;
    @Mock
    private ClientRepository clientRepository;

    @BeforeEach
    public void setUp() {
        client = Client.builder()
                .cardNumber(1234567890123456L)
                .build();
        when(clientRepository.existsByCardNumber(1234567890123456L)).thenReturn(true);
        when(clientRepository.findByCardNumber(1234567890123456L)).thenReturn(client);
    }

    @Test
    void existsByCardNumber_response_true() {
        boolean existsByCardNumber = clientRepository.existsByCardNumber(1234567890123456L);
        assertTrue(existsByCardNumber);
    }

    @Test
    void existsByCardNumber_response_false() {
        boolean existsByCardNumber = clientRepository.existsByCardNumber(1234567890123477L);
        assertFalse(existsByCardNumber);
    }

    @Test
    void findByCardNumber_response_client(){
        Client findByCardNumber = clientRepository.findByCardNumber(1234567890123456L);
        assertNotNull(findByCardNumber);
        assertEquals(1234567890123456L, findByCardNumber.getCardNumber());
    }
    @Test
    void findByCardNumber_response_null(){
        Client findByCardNumber = clientRepository.findByCardNumber(1234567890123477L);
        assertNull(findByCardNumber);
    }
}
