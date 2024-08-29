package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.PetMusicStorage.entity.BankTransactionData;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
public class BankTransactionDataRepositoryTest {
    @Mock
    private BankTransactionDataRepository bankTransactionDataRepository;

    @BeforeEach
    void setUp() {
        BankTransactionData bankTransactionData = BankTransactionData.builder()
                .bankName("Simple_bank")
                .bankUrlTransaction("transaction_url").build();
        when(bankTransactionDataRepository.existsByBankNameIgnoreCase("Simple_bank")).thenReturn(true);
        when(bankTransactionDataRepository.existsByBankUrlTransactionIgnoreCase("transaction_url")).thenReturn(true);
    }

    @Test
    public void existsByBankName_exists() {
        String bankName = "Simple_bank";
        boolean existsBankTransactionalDataByBankName = bankTransactionDataRepository.existsByBankNameIgnoreCase(bankName);

        assertTrue(existsBankTransactionalDataByBankName);
    }

    @Test
    public void existsByBankName_notFound() {
        String bankName = "Another_bank";
        boolean existsBankTransactionalDataByBankName = bankTransactionDataRepository.existsByBankNameIgnoreCase(bankName);

        assertFalse(existsBankTransactionalDataByBankName);
    }

    @Test
    public void existsByBankUrlTransaction_exists() {
        String bankUrl = "transaction_url";
        boolean existsByBankUrlTransaction = bankTransactionDataRepository.existsByBankUrlTransactionIgnoreCase(bankUrl);

        assertTrue(existsByBankUrlTransaction);
    }

    @Test
    public void existsByBankUrlTransaction_notFound() {
        String bankUrl = "Another_url";
        boolean existsByBankUrlTransaction = bankTransactionDataRepository.existsByBankUrlTransactionIgnoreCase(bankUrl);

        assertFalse(existsByBankUrlTransaction);
    }
}
