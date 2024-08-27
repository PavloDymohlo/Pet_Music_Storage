package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private String topic = "Change report in your account";
    @Value("${spring.mail.username}")
    private String from;

    public void sendSimpleMessage(String to, String topic, String text) {
        if (to.isEmpty()){
            log.info("The user did not provide an email address.");
        }else if (!isValidEmail(to)) {
            String errorMessage = "Such an address does not exist.";
            throw new IllegalArgumentException(errorMessage);
        } else {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(topic);
            message.setText(text);
            message.setFrom(from);
            javaMailSender.send(message);
        }
    }

    public boolean isValidEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }
        String domain = email.substring(email.indexOf('@') + 1);
        return isValidDomain(domain);
    }

    private boolean isValidDomain(String domain) {
        try {
            DirContext ictx = new InitialDirContext();
            Attributes attrs = ictx.getAttributes("dns:///" + domain, new String[]{"MX"});
            Attribute attr = attrs.get("MX");
            return attr != null && attr.size() > 0;
        } catch (NamingException e) {
            return false;
        }
    }


    public void notifyUserAboutChangeSubscription(String userEmail, String subscription, String subscriptionExpiredDate) {
        User user = userRepository.findByEmailIgnoreCase(userEmail);
        if (user != null) {
            String message = String.format("Your subscription has been changed to the %s. Subscription expiration date:  %s. If this wasn't you, please contact support.", subscription, subscriptionExpiredDate);
            sendSimpleMessage(userEmail, topic, message);
        }
    }

    public void notifyUserAboutChangePhoneNumber(long newPhoneNumber, String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail);
        if (user != null) {
            String message = String.format("Your phone number has been changed to the %s. If this wasn't you, please contact support.", newPhoneNumber);
            sendSimpleMessage(userEmail, topic, message);
        }
    }

    public void notifyUserAboutChangeBankCard(String userEmail, long newBankCardNumber) {
        User user = userRepository.findByEmailIgnoreCase(userEmail);
        if (user != null) {
            String message = String.format("Your bank card has been changed to the %s. If this wasn't you, please contact support.", newBankCardNumber);
            sendSimpleMessage(userEmail, topic, message);
        }
    }

    public void notifyUserAboutChangePassword(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail);
        if (user != null) {
            String message = String.format("Your password has been changed. If this wasn't you, please contact support.");
            sendSimpleMessage(userEmail, topic, message);
        }
    }

    public void notifyUserAboutChangeAutoRenewStatus(String userEmail, String autoRenewStatus) {
        User user = userRepository.findByEmailIgnoreCase(userEmail);
        if (user != null) {
            String message = String.format("Your auto-renew status has been changed to the %s. If this wasn't you, please contact support.", autoRenewStatus);
            sendSimpleMessage(userEmail, topic, message);
        }
    }

    public void notifyUserAboutChangeEmail(long userPhoneNumber, String oldUserEmail, String newUserEmail) {
        SimpleMailMessage firstSenderMessage = new SimpleMailMessage();
        SimpleMailMessage secondSenderMessage = new SimpleMailMessage();
        User user = userRepository.findByPhoneNumber(userPhoneNumber);
        if (user != null) {
            String messageToOldEmail = String.format("Your email has been changed to the %s. If this wasn't you, please contact support.", newUserEmail);
            String messageToNewEmail = String.format("You have changed your email address. Here is the address of your previous mailbox. %s. If this wasn't you, please contact support.", oldUserEmail);
            firstSenderMessage.setTo(oldUserEmail);
            firstSenderMessage.setSubject(topic);
            firstSenderMessage.setText(messageToOldEmail);
            firstSenderMessage.setFrom(from);
            javaMailSender.send(firstSenderMessage);
            secondSenderMessage.setTo(newUserEmail);
            secondSenderMessage.setSubject(topic);
            secondSenderMessage.setText(messageToNewEmail);
            secondSenderMessage.setFrom(from);
            javaMailSender.send(secondSenderMessage);
        }

    }
    public void notifyUserAboutDeleteAccount(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail);
        if (user != null) {
            String message = String.format("Your account has been deleted. If this wasn't you, please contact support.");
            sendSimpleMessage(userEmail, topic, message);
        }
    }
}
