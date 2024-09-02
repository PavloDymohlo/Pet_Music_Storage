package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @InjectMocks
    private EmailService emailService;
    @Mock
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String fromAddress;
    @Mock
    private UserRepository userRepository;
    @Mock
    private User user;

    @Test
    public void sendSimpleMessage_success() {
        String to = "simple_user@gmail.com";
        String topic = "Topic";
        String text = " Example text";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(topic);
        message.setText(text);
        message.setFrom(fromAddress);

        emailService.sendSimpleMessage(to, topic, text);

        verify(javaMailSender).send(message);
    }

    @Test
    public void notifyUserAboutChangeSubscription_success() {
        String userEmail = "user@example.com";
        String subscription = "FREE";
        String subscriptionExpiredDate = "19 Aug 2024";
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);

        emailService.notifyUserAboutChangeSubscription(userEmail, subscription, subscriptionExpiredDate);

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    public void notifyUserAboutChangePhoneNumber_success() {
        String userEmail = "user@example.com";
        long newPhoneNumber = 80998889955L;
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);

        emailService.notifyUserAboutChangePhoneNumber(newPhoneNumber, userEmail);

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    public void notifyUserAboutChangeBankCard_success() {
        String userEmail = "user@example.com";
        long newBankCardNumber = 80998889955L;
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);

        emailService.notifyUserAboutChangeBankCard(userEmail, newBankCardNumber);

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    public void notifyUserAboutChangePassword_success() {
        String userEmail = "user@example.com";
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);

        emailService.notifyUserAboutChangePassword(userEmail);

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    public void notifyUserAboutChangeAutoRenewStatus_success() {
        String userEmail = "user@example.com";
        String autoRenewStatus = "NO";
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);

        emailService.notifyUserAboutChangeAutoRenewStatus(userEmail, autoRenewStatus);

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    public void notifyUserAboutChangeEmail_success() {
        String newUserEmail = "user@example.com";
        String oldUserEmail = "newEmail@example.com";
        long userPhoneNumber = 80996632587L;
        when(userRepository.findByPhoneNumber(userPhoneNumber)).thenReturn(user);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.notifyUserAboutChangeEmail(userPhoneNumber, oldUserEmail, newUserEmail);

        verify(javaMailSender, times(2)).send(messageCaptor.capture());
        List<SimpleMailMessage> capturedMessages = messageCaptor.getAllValues();
        assertEquals(2, capturedMessages.size());
        SimpleMailMessage firstMessage = capturedMessages.get(0);
        assertEquals(oldUserEmail, firstMessage.getTo()[0]);
        SimpleMailMessage secondMessage = capturedMessages.get(1);
        assertEquals(newUserEmail, secondMessage.getTo()[0]);
    }

    @Test
    public void notifyUserAboutDeleteAccount_success() {
        String userEmail = "user@example.com";
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);

        emailService.notifyUserAboutDeleteAccount(userEmail);

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }
}
