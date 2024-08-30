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
    private UserBankCardRepository userBankCardRepository;

    @BeforeEach
    public void setUp() {
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("28/28")
                .cvv((short) 111).build();
        when(userBankCardRepository.findByCardNumber(1234567890123456L)).thenReturn(userBankCard);
    }

    @Test
    public void findUserBankCardByCardNumber_success() {
        UserBankCard userBankCard = userBankCardRepository.findByCardNumber(1234567890123456L);
        assertNotNull(userBankCard);
    }

    @Test
    public void findUserBankCardByCardNumber_notFound() {
        UserBankCard userBankCard = userBankCardRepository.findByCardNumber(1234567890123400L);

        assertNull(userBankCard);
    }

    @Test
    public void deleteUserBankCardByCardNumber_success() {
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(1234567890123400L).build();
        userBankCardRepository.deleteByCardNumber(1234567890123400L);
        UserBankCard findUserBankCard = userBankCardRepository.findByCardNumber(1234567890123400L);

        assertNull(findUserBankCard);
    }
}
