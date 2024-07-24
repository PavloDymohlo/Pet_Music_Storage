package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.dymohlo.PetMusicStorage.entity.Subscription;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Subscription findBySubscriptionNameIgnoreCase(String subscriptionName);

    boolean existsBySubscriptionNameIgnoreCase(String subscriptionName);

    Subscription findById(long subscriptionId);

    List<Subscription> findBySubscriptionPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    void deleteBySubscriptionNameIgnoreCase(String subscriptionName);
}
