package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.PetMusicStorage.entity.RecipientBankCard;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@DataJpaTest
public class RecipientBankCardRepositoryTest {
    @Mock
    private RecipientBankCardRepository recipientBankCardRepository;

    @BeforeEach
    void setUp() {
        RecipientBankCard recipientBankCard = RecipientBankCard.builder()
                .bankCardName("bankName").build();

        when(recipientBankCardRepository.findByBankCardNameIgnoreCase("bankName")).thenReturn(recipientBankCard);
    }

    @Test
    public void findByBankCardName_success() {
        RecipientBankCard recipientBankCard = recipientBankCardRepository.findByBankCardNameIgnoreCase("bankName");

        assertNotNull(recipientBankCard);
    }

    @Test
    public void findByBankCardName_notFound() {
        RecipientBankCard recipientBankCard = recipientBankCardRepository.findByBankCardNameIgnoreCase("anotherBankName");

        assertNull(recipientBankCard);
    }
}
