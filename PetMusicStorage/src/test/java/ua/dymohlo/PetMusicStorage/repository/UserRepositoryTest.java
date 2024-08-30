package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
public class UserRepositoryTest {
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cvv((short) 111)
                .cardExpirationDate("25/25").build();

        Subscription subscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();

        User user = User.builder()
                .id(1)
                .phoneNumber(80663698520L)
                .email("mockUser@mail.com")
                .password("password")
                .userBankCard(userBankCard)
                .subscription(subscription)
                .telegramChatId("123456").build();
        when(userRepository.findByPhoneNumber(80663698520L)).thenReturn(user);
        when(userRepository.existsByPhoneNumber(80663698520L)).thenReturn(true);
        when(userRepository.existsByEmailIgnoreCase("mockUser@mail.com")).thenReturn(true);
        when(userRepository.findByEmailIgnoreCase("mockUser@mail.com")).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(user);
        when(userRepository.findByTelegramChatId("123456")).thenReturn(user);
    }

    @Test
    public void findUserByPhoneNumber_success() {
        User byPhoneNumber = userRepository.findByPhoneNumber(80663698520L);

        assertNotNull(byPhoneNumber);
    }

    @Test
    public void findUserByPhoneNumber_notFound() {
        User byPhoneNumber = userRepository.findByPhoneNumber(80663698521L);

        assertNull(byPhoneNumber);
    }

    @Test
    public void existsUserByPhoneNumber_exists() {
        boolean phoneNumberExists = userRepository.existsByPhoneNumber(80663698520L);

        assertTrue(phoneNumberExists);
    }

    @Test
    public void existsUserByPhoneNumber_notFound() {
        boolean phoneNumberExists = userRepository.existsByPhoneNumber(80996653211L);

        assertFalse(phoneNumberExists);
    }

    @Test
    public void existsUserByEmail_exists() {
        boolean emailExists = userRepository.existsByEmailIgnoreCase("mockUser@mail.com");

        assertTrue(emailExists);
    }

    @Test
    public void existsUserByEmail_noyFound() {
        boolean emailExists = userRepository.existsByEmailIgnoreCase("anotherUser@mail.com");

        assertFalse(emailExists);
    }

    @Test
    public void findUserByEmail_success() {
        User byEmail = userRepository.findByEmailIgnoreCase("mockUser@mail.com");

        assertNotNull(byEmail);
    }

    @Test
    public void findUserByEmail_notFound() {
        User byEmail = userRepository.findByEmailIgnoreCase("anotherUser@mail.com");

        assertNull(byEmail);
    }

    @Test
    public void findUserById_success() {
        User byId = userRepository.findById(1);

        assertNotNull(byId);
    }

    @Test
    public void findUserById_notFound() {
        User byId = userRepository.findById(2L);

        assertNull(byId);
    }

    @Test
    public void deleteUserById_success() {
        User user = User.builder()
                .id(3L).build();

        userRepository.deleteById(3L);

        User findUser = userRepository.findById(3L);
        assertNull(findUser);
    }

    @Test
    public void findByTelegramChatId_success() {
        User user = userRepository.findByTelegramChatId("123456");

        assertNotNull(user);
    }

    @Test
    public void findByTelegramChatId_notFound() {
        User user = userRepository.findByTelegramChatId("123457");

        assertNull(user);
    }
}
