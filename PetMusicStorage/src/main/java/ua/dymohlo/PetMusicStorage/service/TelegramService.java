package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.dymohlo.PetMusicStorage.configuration.TelegramBot;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;


@Service
@RequiredArgsConstructor
public class TelegramService {
    private final TelegramBot telegramBot;
    private final UserRepository userRepository;

    public void notifyUser(long phoneNumber, String messageTemplate, Object... params) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user != null && user.getTelegramChatId() != null) {
            String message = String.format(messageTemplate, params);
            telegramBot.sendMessage(user.getTelegramChatId(), message);
        }
    }

    public void notifyUserAboutChangeSubscription(long phoneNumber, String subscription, String subscriptionExpiredDate) {
        String messageTemplate = "Your subscription has been changed to %s. Subscription expiration date: %s. " +
                "If this wasn't you, please contact support.";
        notifyUser(phoneNumber, messageTemplate, subscription, subscriptionExpiredDate);
    }

    public void notifyUserAboutChangePhoneNumber(long newPhoneNumber) {
        String messageTemplate = "Your phone number has been changed to %s. If this wasn't you, please contact support.";
        notifyUser(newPhoneNumber, messageTemplate, newPhoneNumber);
    }

    public void notifyUserAboutChangeBankCard(long phoneNumber, String bankCardNumber) {
        String messageTemplate = "Your bank card has been changed to %s. If this wasn't you, please contact support.";
        notifyUser(phoneNumber, messageTemplate, bankCardNumber);
    }

    public void notifyUserAboutChangePassword(long phoneNumber) {
        String messageTemplate = "Your password has been changed. If this wasn't you, please contact support.";
        notifyUser(phoneNumber, messageTemplate);
    }

    public void notifyUserAboutChangeAutoRenewStatus(long phoneNumber, String autoRenewStatus) {
        String messageTemplate = "Your auto-renew status has been changed to %s. If this wasn't you, please contact support.";
        notifyUser(phoneNumber, messageTemplate, autoRenewStatus);
    }

    public void notifyUserAboutChangeEmail(long phoneNumber, String newEmail) {
        String messageTemplate = "Your email has been changed to %s. If this wasn't you, please contact support.";
        notifyUser(phoneNumber, messageTemplate, newEmail);
    }

    public void notifyUserAboutDeleteAccount(String chatId) {
        User user = userRepository.findByTelegramChatId(chatId);
        if (user != null && user.getTelegramChatId() != null) {
            String message = "Your account has been deleted. If this wasn't you, please contact support.";
            telegramBot.sendMessage(user.getTelegramChatId(), message);
        }
    }
}
