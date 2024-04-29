package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
public class UserBankCardRepositoryTest {
    @Mock
    private UserBankCardRepository mockUserBankCardRepository;
    @Mock
    private UserBankCard mockUserBankCard;

    @BeforeEach
    public void setUp() {
        mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("28/28")
                .cvv((short) 111).build();
        when(mockUserBankCardRepository.save(mockUserBankCard)).thenReturn(mockUserBankCard);
        when(mockUserBankCardRepository.findByCardNumber(1234567890123456L)).thenReturn(mockUserBankCard);
    }

    @Test
    public void findByCardNumber_cardNumberExists_returnBankCard() {
        UserBankCard userBankCard = mockUserBankCardRepository.findByCardNumber(1234567890123456L);
        assertNotNull(userBankCard);
        assertEquals(1234567890123456L, userBankCard.getCardNumber());
    }

    @Test
    public void findByCardNumber_cardNumberNotFound_returnNull() {
        UserBankCard userBankCard = mockUserBankCardRepository.findByCardNumber(1234567890123400L);
        when(mockUserBankCardRepository.findByCardNumber(1234567890123400L)).thenReturn(null);
        assertNull(userBankCard);

    }
}
