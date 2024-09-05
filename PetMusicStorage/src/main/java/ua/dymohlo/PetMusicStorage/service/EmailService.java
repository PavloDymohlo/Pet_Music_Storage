package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;

import java.util.function.Consumer;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private String topic = "Change report in your account";
    @Value("${spring.mail.username}")
    private String from;

    public void sendEmail(String to, String messageText) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(topic);
        message.setText(messageText);
        message.setFrom(from);
        try {
            javaMailSender.send(message);
        } catch (MailSendException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private void processUserByEmail(String userEmail, Consumer<User> action) {
        User user = userRepository.findByEmailIgnoreCase(userEmail);
        if (user != null) {
            action.accept(user);
        } else {
            log.warn("User with email {} not found", userEmail);
        }
    }

    public void notifyUserAboutChangeSubscription(String userEmail, String subscription, String subscriptionExpiredDate) {
        processUserByEmail(userEmail, user -> {
            String message = String.format("Your subscription has been changed to %s. Subscription expiration date: %s. If this wasn't you, please contact support.", subscription, subscriptionExpiredDate);
            sendEmail(userEmail, message);
        });
    }

    public void notifyUserAboutChangePhoneNumber(long newPhoneNumber, String userEmail) {
        processUserByEmail(userEmail, user -> {
            String message = String.format("Your phone number has been changed to %s. If this wasn't you, please contact support.", newPhoneNumber);
            sendEmail(userEmail, message);
        });
    }

    public void notifyUserAboutChangeBankCard(String userEmail, long newBankCardNumber) {
        processUserByEmail(userEmail, user -> {
            String message = String.format("Your bank card has been changed to %s. If this wasn't you, please contact support.", newBankCardNumber);
            sendEmail(userEmail, message);
        });
    }

    public void notifyUserAboutChangePassword(String userEmail) {
        processUserByEmail(userEmail, user -> {
            String message = "Your password has been changed. If this wasn't you, please contact support.";
            sendEmail(userEmail, message);
        });
    }

    public void notifyUserAboutChangeAutoRenewStatus(String userEmail, String autoRenewStatus) {
        processUserByEmail(userEmail, user -> {
            String message = String.format("Your auto-renew status has been changed to %s. If this wasn't you, please contact support.", autoRenewStatus);
            sendEmail(userEmail, message);
        });
    }


    public void notifyUserAboutChangeEmail(long userPhoneNumber, String oldUserEmail, String newUserEmail) {
        User user = userRepository.findByPhoneNumber(userPhoneNumber);
        if (user != null) {
            String messageToOldEmail = String.format("Your email has been changed to %s. If this wasn't you, please contact support.", newUserEmail);
            String messageToNewEmail = String.format("You have changed your email address. Previous email: %s. If this wasn't you, please contact support.", oldUserEmail);

            sendEmail(oldUserEmail, messageToOldEmail);
            sendEmail(newUserEmail, messageToNewEmail);
        }
    }

    public void notifyUserAboutDeleteAccount(String userEmail) {
        processUserByEmail(userEmail, user -> {
            String message = "Your account has been deleted. If this wasn't you, please contact support.";
            sendEmail(userEmail, message);
        });
    }
}

