package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.dymohlo.PetMusicStorage.component.TelegramBot;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TelegramService {
    private final TelegramBot telegramBot;
    private final UserRepository userRepository;

    public void notifyUserAboutChangeSubscription(long phoneNumber, String subscription, String subscriptionExpiredDate) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user != null && user.getTelegramChatId() != null) {
            String message = String.format("Your subscription has been changed to the %s. Subscription expiration date:  %s. If this wasn't you, please contact support.", subscription, subscriptionExpiredDate);
            telegramBot.sendMessage(user.getTelegramChatId(), message);
        }
    }

    public void notifyUserAboutChangePhoneNumber(long newPhoneNumber) {
        User user = userRepository.findByPhoneNumber(newPhoneNumber);
        if (user != null && user.getTelegramChatId() != null) {
            String message = String.format("Your phone number has been changed to the %s. " +
                    "If this wasn't you, please contact support.", newPhoneNumber);
            telegramBot.sendMessage(user.getTelegramChatId(), message);
        }
    }

    public void notifyUserAboutChangeBankCard(long phoneNumber, String bankCardNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user != null && user.getTelegramChatId() != null) {
            String message = String.format("Your bank card has been changed to the %s. " +
                    "If this wasn't you, please contact support.", bankCardNumber);
            telegramBot.sendMessage(user.getTelegramChatId(), message);
        }
    }

    public void notifyUserAboutChangePassword(long phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user != null && user.getTelegramChatId() != null) {
            String message = String.format("Your password has been changed. If this wasn't you, please contact support.");
            telegramBot.sendMessage(user.getTelegramChatId(), message);
        }
    }

    public void notifyUserAboutChangeAutoRenewStatus(long phoneNumber, String autoRenewStatus) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user != null && user.getTelegramChatId() != null) {
            String message = String.format("Your auto-renew status has been changed to the %s. " +
                    "If this wasn't you, please contact support.", autoRenewStatus);
            telegramBot.sendMessage(user.getTelegramChatId(), message);
        }
    }

    public void notifyUserAboutChangeEmail(long phoneNumber, String newEmail) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user != null && user.getTelegramChatId() != null) {
            String message = String.format("Your email has been changed to the %s. If this wasn't you, please contact support.", newEmail);
            telegramBot.sendMessage(user.getTelegramChatId(), message);
        }
    }

    public void notifyUserAboutDeleteAccount(String chatId) {
        User user = userRepository.findByTelegramChatId(chatId);
        if (user != null && user.getTelegramChatId() != null) {
            String message = String.format("Your account has been deleted. If this wasn't you, please contact support.");
            telegramBot.sendMessage(user.getTelegramChatId(), message);
        }
    }
}