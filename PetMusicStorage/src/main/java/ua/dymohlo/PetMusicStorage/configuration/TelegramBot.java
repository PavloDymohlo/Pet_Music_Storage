package ua.dymohlo.PetMusicStorage.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final UserRepository userRepository;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();
            if ("/start".equals(text)) {
                handleStartCommand(chatId);
            } else {
                handlePhoneNumberInput(chatId, text);
            }
        }
    }

    private void handleStartCommand(String chatId) {
        sendMessage(chatId, "Enter your phone number:");
    }

    private void handlePhoneNumberInput(String chatId, String text) {
        try {
            long phoneNumber = Long.parseLong(text);
            User user = userRepository.findByPhoneNumber(phoneNumber);
            if (user != null) {
                updateAndNotifyUser(user, chatId);
            } else {
                sendMessage(chatId, "User with phone number " + phoneNumber + " not found!");
            }
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Please, enter correct phone number");
        }
    }

    private void updateAndNotifyUser(User user, String chatId) {
        user.setTelegramChatId(chatId);
        userRepository.save(user);
        sendMessage(chatId, "Congratulations! You've successfully connected to our bot. " +
                "You will now receive notifications related to changes in your account.");
    }

    @Override
    public String getBotUsername() {
        final String botUserName = "musicStorageMessage_bot";
        return botUserName;
    }

    @Override
    public String getBotToken() {
        final String botToken = "7387041594:AAHxs3a1ZL3NzLOEYNHBHISV0r05g2_b4gc";
        return botToken;
    }

    public void sendMessage(String chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}