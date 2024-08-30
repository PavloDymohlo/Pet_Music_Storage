package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.dymohlo.PetMusicStorage.component.TelegramBot;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TelegramServiceTest {
    @InjectMocks
    private TelegramService telegramService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TelegramBot telegramBot;
    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        user.setTelegramChatId("123456");

        when(user.getTelegramChatId()).thenReturn("123456");
    }

    @Test
    public void notifyUserAboutChangeSubscription_success() {
        long phoneNumber = 80663214785L;
        String subscription = "FREE";
        String subscriptionExpiredDate = "19 sept 2024";
        String message = String.format("Your subscription has been changed to the %s. Subscription expiration date:  %s. " +
                "If this wasn't you, please contact support.", subscription, subscriptionExpiredDate);
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(user);

        telegramService.notifyUserAboutChangeSubscription(phoneNumber, subscription, subscriptionExpiredDate);

        verify(telegramBot).sendMessage(user.getTelegramChatId(), message);
    }

    @Test
    public void notifyUserAboutChangePhoneNumber_success() {
        long newPhoneNumber = 80663214785L;
        String message = String.format("Your phone number has been changed to the %s. " +
                "If this wasn't you, please contact support.", newPhoneNumber);
        when(userRepository.findByPhoneNumber(newPhoneNumber)).thenReturn(user);

        telegramService.notifyUserAboutChangePhoneNumber(newPhoneNumber);

        verify(telegramBot).sendMessage(user.getTelegramChatId(), message);
    }

    @Test
    public void notifyUserAboutChangeBankCard_success() {
        long newPhoneNumber = 80663214785L;
        String bankCardNumber = "1234567890123456";
        String message = String.format("Your bank card has been changed to the %s. " +
                "If this wasn't you, please contact support.", bankCardNumber);
        when(userRepository.findByPhoneNumber(newPhoneNumber)).thenReturn(user);

        telegramService.notifyUserAboutChangeBankCard(newPhoneNumber, bankCardNumber);

        verify(telegramBot).sendMessage(user.getTelegramChatId(), message);
    }

    @Test
    public void notifyUserAboutChangePassword_success() {
        long newPhoneNumber = 80663214785L;
        String message = String.format("Your password has been changed. If this wasn't you, please contact support.");
        when(userRepository.findByPhoneNumber(newPhoneNumber)).thenReturn(user);

        telegramService.notifyUserAboutChangePassword(newPhoneNumber);

        verify(telegramBot).sendMessage(user.getTelegramChatId(), message);
    }

    @Test
    public void notifyUserAboutChangeAutoRenewStatus_success() {
        long newPhoneNumber = 80663214785L;
        String autoRenewStatus = "NO";
        String message = String.format("Your auto-renew status has been changed to the %s. " +
                "If this wasn't you, please contact support.", autoRenewStatus);
        when(userRepository.findByPhoneNumber(newPhoneNumber)).thenReturn(user);

        telegramService.notifyUserAboutChangeAutoRenewStatus(newPhoneNumber, autoRenewStatus);

        verify(telegramBot).sendMessage(user.getTelegramChatId(), message);
    }

    @Test
    public void notifyUserAboutChangeEmail_success() {
        long newPhoneNumber = 80663214785L;
        String newEmail = "new_email@example.com";
        String message = String.format("Your email has been changed to the %s. If this wasn't you, please contact support.", newEmail);
        when(userRepository.findByPhoneNumber(newPhoneNumber)).thenReturn(user);

        telegramService.notifyUserAboutChangeEmail(newPhoneNumber, newEmail);

        verify(telegramBot).sendMessage(user.getTelegramChatId(), message);
    }

    @Test
    public void notifyUserAboutDeleteAccount_success() {
        String chatId = "123456";
        String message = String.format("Your account has been deleted. If this wasn't you, please contact support.");
        when(userRepository.findByTelegramChatId(chatId)).thenReturn(user);

        telegramService.notifyUserAboutDeleteAccount(chatId);

        verify(telegramBot).sendMessage(user.getTelegramChatId(), message);
    }
}
