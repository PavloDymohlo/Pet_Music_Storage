package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;

import ua.dymohlo.PetMusicStorage.repository.UserBankCardRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserBankCardServiceTest {
    @InjectMocks
    private UserBankCardService userBankCardService;
    @Mock
    private UserBankCardRepository mockUserBankCardRepository;
    @Mock
    private UserBankCard mockUserBankCard;

    @Test
    public void validateBankCard_return_true() {
        UserBankCard mockUserBankCard = UserBankCard.builder()
                .cardNumber(8965214556987456L)
                .cardExpirationDate("26/26")
                .cvv((short) 123L).build();
        UserBankCard findUserBankCard = UserBankCard.builder()
                .cardNumber(8965214556987456L)
                .cardExpirationDate("26/26")
                .cvv((short) 123L).build();
        when(mockUserBankCardRepository.findByCardNumber(findUserBankCard.getCardNumber())).thenReturn(mockUserBankCard);

        boolean validate = userBankCardService.validateBankCard(findUserBankCard);

        assertTrue(validate);
        verify(mockUserBankCardRepository).findByCardNumber(findUserBankCard.getCardNumber());
    }

    @Test
    public void validateBankCard_return_false() {
        UserBankCard findUserBankCard = UserBankCard.builder()
                .cardNumber(8965214556987456L)
                .cardExpirationDate("26/26")
                .cvv((short) 123L).build();
        when(mockUserBankCardRepository.findByCardNumber(findUserBankCard.getCardNumber())).thenReturn(null);

        boolean validate = userBankCardService.validateBankCard(findUserBankCard);

        assertFalse(validate);
        verify(mockUserBankCardRepository).findByCardNumber(findUserBankCard.getCardNumber());
    }

    @Test
    public void deleteBankCard_success() {
        long bankCardNumber = 8965214556987456L;

        userBankCardService.deleteBankCard(bankCardNumber);

        verify(mockUserBankCardRepository).deleteByCardNumber(bankCardNumber);
    }

    @Test
    public void checkBankCardUsers_return_size() {
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(8965214556987456L)
                .cardExpirationDate("26/26")
                .cvv((short) 123L)
                .build();
        User user = User.builder()
                .userBankCard(userBankCard)
                .build();
        userBankCard.setUsers(Collections.singletonList(user));
        long findUserBankCardNumber = 8965214556987456L;
        when(mockUserBankCardRepository.findByCardNumber(findUserBankCardNumber)).thenReturn(userBankCard);

        int result = userBankCardService.checkBankCardUsers(findUserBankCardNumber);

        assertEquals(1, result);
    }
}
