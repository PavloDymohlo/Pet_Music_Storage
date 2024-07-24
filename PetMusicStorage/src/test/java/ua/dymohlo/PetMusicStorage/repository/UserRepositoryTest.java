package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
public class UserRepositoryTest {
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private UserBankCardRepository mockUserBankCardRepository;
    @Mock
    private SubscriptionRepository mockSubscriptionRepository;
    @Mock
    private User mockUser;
    @Mock
    private UserBankCard mockUserBankCard;
    @Mock
    private Subscription mockSubscription;

    @BeforeEach
    public void setUp() {
        mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cvv((short) 111)
                .cardExpirationDate("25/25").build();
        when(mockUserBankCardRepository.save(mockUserBankCard)).thenReturn(mockUserBankCard);

        mockSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        when(mockSubscriptionRepository.save(mockSubscription)).thenReturn(mockSubscription);

        mockUser = User.builder()
                .id(1)
                .phoneNumber(80663698520L)
                .email("mockUser@mail.com")
                .password("password")
                .userBankCard(mockUserBankCard)
                .subscription(mockSubscription).build();
        when(mockUserRepository.save(mockUser)).thenReturn(mockUser);
        when(mockUserRepository.findByPhoneNumber(80663698520L)).thenReturn(mockUser);
        when(mockUserRepository.findByEmailIgnoreCase("mockUser@mail.com")).thenReturn(mockUser);
        when(mockUserRepository.findById(1)).thenReturn(mockUser);
    }

    @Test
    public void findByPhoneNumber_phoneNumberExists_returnsPhoneNumber() {
        User byPhoneNumber = mockUserRepository.findByPhoneNumber(80663698520L);
        assertNotNull(byPhoneNumber);
        assertEquals(80663698520L, byPhoneNumber.getPhoneNumber());
    }

    @Test
    public void findByPhoneNumber_phoneNumberNotFound_returnNull() {
        User byPhoneNumber = mockUserRepository.findByPhoneNumber(80663698521L);
        when(mockUserRepository.findByPhoneNumber(80663698521L)).thenReturn(null);
        assertNull(byPhoneNumber);
    }

    @Test
    public void existsByPhoneNumber_phoneNumberExists_true() {
        long phoneNumber = 80663698520L;
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        boolean phoneNumberExists = mockUserRepository.existsByPhoneNumber(phoneNumber);

        assertTrue(phoneNumberExists, "User with the phone number exist");
    }

    @Test
    public void existsByPhoneNumber_phoneNumberNotExists_false() {
        long phoneNumber = 80996653200L;
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);

        boolean phoneNumberExists = mockUserRepository.existsByPhoneNumber(phoneNumber);

        assertFalse(phoneNumberExists, "User with  phone number should not exist");
    }

    @Test
    public void existsByEmail_emailExists_true() {
        String email = "mockUser@mail.com";
        when(mockUserRepository.existsByEmailIgnoreCase(email)).thenReturn(true);

        boolean emailExists = mockUserRepository.existsByEmailIgnoreCase(email);

        assertTrue(emailExists, "User with the email exist");
    }

    @Test
    public void existsByEmail_emailExists_false() {
        String email = "mockUser@mail.com";
        when(mockUserRepository.existsByEmailIgnoreCase(email)).thenReturn(false);

        boolean emailExists = mockUserRepository.existsByEmailIgnoreCase(email);

        assertFalse(emailExists, "User with email should not exist");
    }

    @Test
    public void findByEmail_emailExists_returnEmail() {
        User byEmail = mockUserRepository.findByEmailIgnoreCase("mockUser@mail.com");
        assertNotNull(byEmail);
        assertEquals("mockUser@mail.com", byEmail.getEmail());
    }

    @Test
    public void findByEmail_emailNotExists_returnNull() {
        User byEmail = mockUserRepository.findByEmailIgnoreCase("mockUser2@mail.com");
        assertNull(byEmail);
    }

    @Test
    public void findById_idExists_returnId() {
        User byId = mockUserRepository.findById(1);
        assertNotNull(byId);
        assertEquals(1, byId.getId());
    }

    @Test
    public void findById_idNoExists_returnNull() {
        User byId = mockUserRepository.findById(2);
        assertNull(byId);
    }

    @Test
    public void deleteById_idExists_deleteUser() {
        long userId = 10L;

        mockUserRepository.deleteById(userId);

        verify(mockUserRepository, times(1)).deleteById(userId);
    }

    @Test
    public void deleteById__idNoExists_doNothing() {
        long userId = 11L;
        when(mockUserRepository.findById(userId)).thenReturn(null);
        Optional<User> userOptional = Optional.ofNullable(mockUserRepository.findById(userId));

        if (userOptional.isPresent()) {
            mockUserRepository.deleteById(userId);
        }

        verify(mockUserRepository, never()).deleteById(userId);
    }
}
