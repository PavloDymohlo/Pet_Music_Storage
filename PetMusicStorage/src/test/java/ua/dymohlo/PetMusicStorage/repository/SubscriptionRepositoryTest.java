package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.PetMusicStorage.entity.Subscription;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
public class SubscriptionRepositoryTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;


    @BeforeEach
    public void setUp() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .subscriptionName("MAXIMUM").build();
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("MAXIMUM")).thenReturn(subscription);
        when(subscriptionRepository.existsBySubscriptionNameIgnoreCase("MAXIMUM")).thenReturn(true);
        when(subscriptionRepository.findById(1L)).thenReturn(subscription);
        when(subscriptionRepository.findBySubscriptionPriceBetween(BigDecimal.valueOf(0), BigDecimal.valueOf(150)))
                .thenReturn(subscriptions);
    }

    @Test
    public void findBySubscriptionName_success() {
        Subscription findBySubscriptionName = subscriptionRepository.findBySubscriptionNameIgnoreCase("MAXIMUM");

        assertNotNull(findBySubscriptionName);
    }

    @Test
    public void findBySubscriptionName_notFound() {
        Subscription findBySubscriptionName = subscriptionRepository.findBySubscriptionNameIgnoreCase("PREMIUM");

        assertNull(findBySubscriptionName);
    }

    @Test
    public void existsBySubscriptionName_exists() {
        boolean subscriptionExists = subscriptionRepository.existsBySubscriptionNameIgnoreCase("MAXIMUM");

        assertTrue(subscriptionExists);
    }

    @Test
    public void existsBySubscriptionName_notFound() {
        boolean subscriptionExists = subscriptionRepository.existsBySubscriptionNameIgnoreCase("PREMIUM");

        assertFalse(subscriptionExists);
    }

    @Test
    public void findSubscriptionById_success() {
        Subscription subscription = subscriptionRepository.findById(1L);

        assertNotNull(subscription);
    }

    @Test
    public void findSubscriptionById_notFound() {
        Subscription subscription = subscriptionRepository.findById(2L);

        assertNull(subscription);
    }

    @Test
    public void findBySubscriptionPriceBetween_success() {
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriptionPriceBetween(BigDecimal.valueOf(0), BigDecimal.valueOf(150));

        assertFalse(subscriptions.isEmpty());
    }

    @Test
    public void findBySubscriptionPriceBetween_notFound() {
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriptionPriceBetween(BigDecimal.valueOf(0), BigDecimal.valueOf(50));

        assertTrue(subscriptions.isEmpty());
    }

    @Test
    public void deleteBySubscriptionName_success() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("PREMIUM")
                .build();
        subscriptionRepository.save(subscription);
        subscriptionRepository.deleteBySubscriptionNameIgnoreCase("PREMIUM");
        Subscription subscription1 = subscriptionRepository.findBySubscriptionNameIgnoreCase("PREMIUM");

        assertNull(subscription1);
    }
}