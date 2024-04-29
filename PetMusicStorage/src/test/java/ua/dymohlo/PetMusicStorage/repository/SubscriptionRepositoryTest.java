package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.PetMusicStorage.entity.Subscription;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
public class SubscriptionRepositoryTest {
    @Mock
    private SubscriptionRepository mockSubscriptionRepository;
    @Mock
    private Subscription mockSubscription;

    @BeforeEach
    public void setUp() {
        mockSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        when(mockSubscriptionRepository.save(mockSubscription)).thenReturn(mockSubscription);
        when(mockSubscriptionRepository.findBySubscriptionName("MAXIMUM")).thenReturn(mockSubscription);
    }

    @Test
    public void findBySubscriptionName_subscriptionExists_returnSubscription() {
        Subscription findBySubscriptionName = mockSubscriptionRepository.findBySubscriptionName("MAXIMUM");
        assertNotNull(findBySubscriptionName);
        assertEquals("MAXIMUM", findBySubscriptionName.getSubscriptionName());
    }

    @Test
    public void findBySubscriptionName_subscriptionNotFound_returnNull() {
        Subscription findBySubscriptionName = mockSubscriptionRepository.findBySubscriptionName("PREMIUM");
        when(mockSubscriptionRepository.findBySubscriptionName("PREMIUM")).thenReturn(null);
        assertNull(findBySubscriptionName);
    }

    @Test
    public void existsBySubscriptionName_SubscriptionExists_true() {
        Subscription subscription = Subscription.builder().subscriptionName("MAXIMUM").build();
        when(mockSubscriptionRepository.existsBySubscriptionName(String.valueOf(subscription))).thenReturn(true);
        boolean subscriptionExists = mockSubscriptionRepository.existsBySubscriptionName(String.valueOf(subscription));
        assertTrue(subscriptionExists);
    }

    @Test
    public void existsBySubscriptionName_SubscriptionNotFound_false() {
        Subscription subscription = Subscription.builder().subscriptionName("PREMIUM").build();
        when(mockSubscriptionRepository.existsBySubscriptionName(String.valueOf(subscription))).thenReturn(false);
        boolean subscriptionExists = mockSubscriptionRepository.existsBySubscriptionName(String.valueOf(subscription));
        assertFalse(subscriptionExists);
    }

}
