package ua.dymohlo.PetMusicStorage.component;

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
        String chatId = update.getMessage().getChatId().toString();
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            if ("/start".equals(text)) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Enter your phone number:");
                try {
                    this.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    long phoneNumber = Long.parseLong(text);
                    User user = userRepository.findByPhoneNumber(phoneNumber);
                    if (user != null) {
                        user.setTelegramChatId(chatId);
                        userRepository.save(user);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Congratulations! You've successfully connected to our bot. You will now receive notifications related to changes in your account.");

                        try {
                            this.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    } else {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("User with phone number "+ phoneNumber+" not found!");
                        try {
                            this.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                }
                catch (NumberFormatException e) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Please, enter correct phone number");
                    try {
                        this.execute(sendMessage);
                    } catch (TelegramApiException exception) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }




    @Override
    public String getBotUsername() {
        return "musicStorageMessage_bot";
    }
    @Override
    public String getBotToken() {
        return "7387041594:AAHxs3a1ZL3NzLOEYNHBHISV0r05g2_b4gc";
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}



