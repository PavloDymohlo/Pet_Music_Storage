package ua.dymohlo.MockBankSystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.dymohlo.MockBankSystem.entity.Client;
import ua.dymohlo.MockBankSystem.repository.ClientRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {
    @InjectMocks
    private ClientService clientService;
    @Mock
    private ClientRepository clientRepository;
    @Test
    public void testTransactionMoney_Success() {
        long outputCardNumber = 1234567890123456L;
        long targetCardNumber = 6543210987654321L;
        int sum = 100;
        String cardExpirationDate = "12/25";
        short cvv = 123;

        Client payer = new Client();
        payer.setBalance(200);
        payer.setCardExpirationDate("12/25");
        payer.setCvv(cvv);

        Client recipient = new Client();
        recipient.setBalance(100);

        when(clientRepository.existsByCardNumber(outputCardNumber)).thenReturn(true);
        when(clientRepository.findByCardNumber(outputCardNumber)).thenReturn(payer);
        when(clientRepository.findByCardNumber(targetCardNumber)).thenReturn(recipient);

        clientService.transactionMoney(outputCardNumber, targetCardNumber, sum, cardExpirationDate, cvv);

        verify(clientRepository).save(payer);
        verify(clientRepository).save(recipient);
        assert(payer.getBalance() == 100);
        assert(recipient.getBalance() == 200);
    }

    @Test
    public void testTransactionMoney_InsufficientFunds(){
        long outputCardNumber = 1234567890123456L;
        long targetCardNumber = 6543210987654321L;
        int sum = 300;
        String cardExpirationDate = "12/25";
        short cvv = 123;

        Client payer = new Client();
        payer.setBalance(200);
        payer.setCardExpirationDate("12/25");
        payer.setCvv(cvv);

        Client recipient = new Client();
        recipient.setBalance(100);

        when(clientRepository.existsByCardNumber(outputCardNumber)).thenReturn(true);
        when(clientRepository.findByCardNumber(outputCardNumber)).thenReturn(payer);
        when(clientRepository.findByCardNumber(targetCardNumber)).thenReturn(recipient);

        assertThrows(IllegalArgumentException.class, () -> {
            clientService.transactionMoney(outputCardNumber, targetCardNumber, sum, cardExpirationDate, cvv);
        });

        verify(clientRepository, never()).save(payer);
        verify(clientRepository, never()).save(recipient);
    }

    @Test
    public void testTransactionMoney_InvalidCardData(){
        long outputCardNumber = 1234567890123456L;
        long targetCardNumber = 6543210987654321L;
        int sum = 100;
        String cardExpirationDate = "12/25";
        short cvv = 123;

        when(clientRepository.existsByCardNumber(outputCardNumber)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            clientService.transactionMoney(outputCardNumber, targetCardNumber, sum, cardExpirationDate, cvv);
        });

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    public void testTransactionMoney_ExpiredCard(){
        long outputCardNumber = 1234567890123456L;
        long targetCardNumber = 6543210987654321L;
        int sum = 100;
        String cardExpirationDate = "12/20";
        short cvv = 123;

        Client payer = new Client();
        payer.setBalance(200);
        payer.setCardExpirationDate("12/20");
        payer.setCvv(cvv);

        when(clientRepository.existsByCardNumber(outputCardNumber)).thenReturn(true);
        when(clientRepository.findByCardNumber(outputCardNumber)).thenReturn(payer);

        assertThrows(IllegalArgumentException.class, () -> {
            clientService.transactionMoney(outputCardNumber, targetCardNumber, sum, cardExpirationDate, cvv);
        });

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    public void testTransactionMoney_InvalidCvv(){
        long outputCardNumber = 1234567890123456L;
        long targetCardNumber = 6543210987654321L;
        int sum = 100;
        String cardExpirationDate = "12/25";
        short cvv = 999;

        Client payer = new Client();
        payer.setBalance(200);
        payer.setCardExpirationDate("12/25");
        payer.setCvv((short) 123);

        when(clientRepository.existsByCardNumber(outputCardNumber)).thenReturn(true);
        when(clientRepository.findByCardNumber(outputCardNumber)).thenReturn(payer);

        assertThrows(IllegalArgumentException.class, () -> {
            clientService.transactionMoney(outputCardNumber, targetCardNumber, sum, cardExpirationDate, cvv);
        });

        verify(clientRepository, never()).save(any(Client.class));
    }
}
