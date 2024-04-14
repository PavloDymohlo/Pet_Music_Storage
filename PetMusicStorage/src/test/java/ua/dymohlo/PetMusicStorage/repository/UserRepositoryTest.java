package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
}
