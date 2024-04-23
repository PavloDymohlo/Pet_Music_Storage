package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
    public void setUser() {
        mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cvv((short) 111)
                .cardExpirationDate("25/25").build();
        when(mockUserBankCardRepository.save(mockUserBankCard)).thenReturn(mockUserBankCard);

        mockSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        when(mockSubscriptionRepository.save(mockSubscription)).thenReturn(mockSubscription);

        mockUser = User.builder()
                .phoneNumber(80663698520L)
                .password("password")
                .userBankCard(mockUserBankCard)
                .subscription(mockSubscription).build();
        when(mockUserRepository.save(mockUser)).thenReturn(mockUser);
        when(mockUserRepository.findByPhoneNumber(80663698520L)).thenReturn(mockUser);
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
    public void existsByPhoneNumber_phoneNumberExists_true(){
        long phoneNumber = 80663698520L;
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        boolean exists = mockUserRepository.existsByPhoneNumber(phoneNumber);

        assertTrue(exists, "User with the phone number exist");
    }
    @Test
    public void existsByPhoneNumber_phoneNumberNotExists_false() {
        long nonExistentPhoneNumber = 1234567890L;
        when(mockUserRepository.existsByPhoneNumber(nonExistentPhoneNumber)).thenReturn(false);

        boolean exists = mockUserRepository.existsByPhoneNumber(nonExistentPhoneNumber);

        assertFalse(exists, "User with  phone number should not exist");
    }
    @Test
    public void existsByEmail_EmailExists_true(){
        String email = "mockUser@mail.com";
        when(mockUserRepository.existsByEmail(email)).thenReturn(true);

        boolean exists = mockUserRepository.existsByEmail(email);

        assertTrue(exists, "User with the email exist");
    }
    @Test
    public void existsByEmail_EmailExists_false() {
        String email = "mockUser@mail.com";
        when(mockUserRepository.existsByEmail(email)).thenReturn(false);

        boolean exists = mockUserRepository.existsByEmail(email);

        assertFalse(exists, "User with email should not exist");
    }
}
