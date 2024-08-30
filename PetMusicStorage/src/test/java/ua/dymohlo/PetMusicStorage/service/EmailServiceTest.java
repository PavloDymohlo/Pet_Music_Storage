package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

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

    @Test
    public void sendSimpleMessage_success(){
        String to = "simple_user@gmail.com";
        String topic = "Topic";
        String text = " Example text";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(topic);
        message.setText(text);
        message.setFrom(fromAddress);

        emailService.sendSimpleMessage(to,topic,text);

        verify(javaMailSender).send(message);
    }

    @Test
    public void sendSimpleMessage_incorrectEmail(){
        String to = "simple_user@mailcom";
        String topic = "Topic";
        String text = " Example text";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(topic);
        message.setText(text);
        message.setFrom(fromAddress);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,()->{
            emailService.sendSimpleMessage(to, topic, text);
        });

        assertEquals("Such an address does not exist.",exception.getMessage());
    }
}
