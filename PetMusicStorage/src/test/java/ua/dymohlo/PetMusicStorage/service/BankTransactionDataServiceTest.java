package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.dymohlo.PetMusicStorage.dto.NewBankTransactionDataDTO;
import ua.dymohlo.PetMusicStorage.entity.BankTransactionData;
import ua.dymohlo.PetMusicStorage.repository.BankTransactionDataRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankTransactionDataServiceTest {
    @InjectMocks
    private BankTransactionDataService bankTransactionDataService;
    @Mock
    private BankTransactionDataRepository bankTransactionDataRepository;

    @Test
    public void addBankTransactionData_success() {
        NewBankTransactionDataDTO newBankTransactionDataDTO = NewBankTransactionDataDTO.builder()
                .bankName("Simple_Bank")
                .bankUrlTransaction("simple)bank_url").build();

        when(bankTransactionDataRepository.existsByBankNameIgnoreCase("Simple_Bank")).thenReturn(false);
        when(bankTransactionDataRepository.existsByBankUrlTransactionIgnoreCase("simple)bank_url")).thenReturn(false);
        BankTransactionData newBankTransactionData = BankTransactionData.builder()
                .bankName("Simple_Bank")
                .bankUrlTransaction("simple)bank_url").build();
        bankTransactionDataService.addBankTransactionData(newBankTransactionDataDTO);

        verify(bankTransactionDataRepository).save(newBankTransactionData);
    }

    @Test
    public void addBankTransactionData_bankDataAlreadyExists() {
        NewBankTransactionDataDTO newBankTransactionDataDTO = NewBankTransactionDataDTO.builder()
                .bankName("Simple_Bank")
                .bankUrlTransaction("simple)bank_url").build();
        String bankName = newBankTransactionDataDTO.getBankName();

        when(bankTransactionDataRepository.existsByBankNameIgnoreCase("Simple_Bank")).thenReturn(true);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bankTransactionDataService.addBankTransactionData(newBankTransactionDataDTO);
        });

        assertEquals("Bank with bank's name " + bankName + " already exists", exception.getMessage());
    }
}
