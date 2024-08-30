package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;

import ua.dymohlo.PetMusicStorage.repository.UserBankCardRepository;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserBankCardServiceTest {
    @InjectMocks
    private UserBankCardService userBankCardService;
    @Mock
    private UserBankCardRepository userBankCardRepository;
    @Mock
    private UserBankCard userBankCard;

    @BeforeEach
    void setUp() {
        userBankCard = UserBankCard.builder()
                .cardNumber(8965214556987456L)
                .cardExpirationDate("26/26")
                .cvv((short) 123L).build();
    }

    @Test
    public void validateBankCard_true() {

        when(userBankCardRepository.findByCardNumber(8965214556987456L)).thenReturn(userBankCard);

        boolean validate = userBankCardService.validateBankCard(userBankCard);

        assertTrue(validate);
    }

    @Test
    public void validateBankCard_expirationDateMismatch() {
        UserBankCard existingCard = UserBankCard.builder()
                .cardNumber(8965214556987456L)
                .cardExpirationDate("12/25") 
                .cvv((short) 123).build();

        when(userBankCardRepository.findByCardNumber(8965214556987456L)).thenReturn(existingCard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userBankCardService.validateBankCard(userBankCard);
        });

        assertEquals("Bank card with number " + userBankCard.getCardNumber() +
                " already exists, but card expiration date is invalid", exception.getMessage());
    }

    @Test
    public void deleteBankCard_success() {
        long bankCardNumber = 8965214556987456L;

        userBankCardService.deleteBankCard(bankCardNumber);

        verify(userBankCardRepository).deleteByCardNumber(bankCardNumber);
    }

    @Test
    public void checkBankCardUsers_returnSize() {
        User user = User.builder()
                .userBankCard(userBankCard)
                .build();
        userBankCard.setUsers(Collections.singletonList(user));
        long findUserBankCardNumber = 8965214556987456L;
        when(userBankCardRepository.findByCardNumber(findUserBankCardNumber)).thenReturn(userBankCard);

        int result = userBankCardService.checkBankCardUsers(findUserBankCardNumber);

        assertEquals(1, result);
    }
}