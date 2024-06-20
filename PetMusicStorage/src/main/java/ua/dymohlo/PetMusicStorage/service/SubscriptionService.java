package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.dymohlo.PetMusicStorage.dto.NewSubscriptionDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateSubscriptionDurationTimeDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateSubscriptionNameDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateSubscriptionPriceDTO;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public void addNewSubscription(NewSubscriptionDTO newSubscriptionDTO) {
        if (subscriptionRepository.existsBySubscriptionName(newSubscriptionDTO.getSubscriptionName())) {
            throw new IllegalArgumentException("Subscription with name " + newSubscriptionDTO.getSubscriptionName() + " already exists");
        }
        Subscription newSubscription = Subscription.builder()
                .subscriptionName(newSubscriptionDTO.getSubscriptionName())
                .subscriptionPrice(newSubscriptionDTO.getSubscriptionPrice())
                .subscriptionDurationTime(newSubscriptionDTO.getSubscriptionDurationTime()).build();

        subscriptionRepository.save(newSubscription);
    }

    public Subscription findSubscriptionById(long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with id " + subscriptionId + " not found");
        }
        return subscription;
    }

    public List<Subscription> findAllSubscription() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        if (subscriptions.isEmpty()) {
            throw new NoSuchElementException("Subscriptions not found");
        }
        return subscriptions;
    }

    public List<Subscription> findSubscriptionsByPrice(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriptionPriceBetween(minPrice, maxPrice);
        if (subscriptions.isEmpty()) {
            throw new NoSuchElementException("Subscriptions between price " + minPrice + " and " + maxPrice + " not found");
        }
        return subscriptions;
    }

    public Subscription findSubscriptionBySubscriptionName(String subscriptionName) {
        Subscription subscription = subscriptionRepository.findBySubscriptionName(subscriptionName);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + subscriptionName + " not found");
        }
        return subscription;
    }

    public void updateSubscriptionName(UpdateSubscriptionNameDTO updateSubscriptionNameDTO) {
        Subscription subscription = subscriptionRepository.findBySubscriptionName(updateSubscriptionNameDTO.getCurrentSubscriptionName());
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + updateSubscriptionNameDTO.getCurrentSubscriptionName() + " not found");
        }
        subscription.setSubscriptionName(updateSubscriptionNameDTO.getNewSubscriptionName());
        subscriptionRepository.save(subscription);
    }

    public void updateSubscriptionPrice(UpdateSubscriptionPriceDTO updateSubscriptionPriceDTO) {
        Subscription subscription = subscriptionRepository.findBySubscriptionName(updateSubscriptionPriceDTO.getSubscriptionName());
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + updateSubscriptionPriceDTO.getSubscriptionName() + " not found");
        }
        subscription.setSubscriptionPrice(updateSubscriptionPriceDTO.getNewPrice());
        subscriptionRepository.save(subscription);
    }

    public void updateSubscriptionDurationTime(UpdateSubscriptionDurationTimeDTO updateSubscriptionDurationTimeDTO) {
        Subscription subscription = subscriptionRepository.findBySubscriptionName(updateSubscriptionDurationTimeDTO.getSubscriptionName());
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + updateSubscriptionDurationTimeDTO.getSubscriptionName() + " not found");
        }
        subscription.setSubscriptionDurationTime(updateSubscriptionDurationTimeDTO.getNewDurationTime());
        subscriptionRepository.save(subscription);
    }

    public void deleteSubscriptionById(long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with id " + subscriptionId + " not found");
        }
        if (!subscription.getUsers().isEmpty()) {
            throw new IllegalArgumentException("Subscription with id " + subscriptionId + " has users and cannot be deleted");
        }
        subscriptionRepository.deleteById(subscriptionId);
    }

    @Transactional
    public void deleteSubscriptionBySubscriptionName(String subscriptionName) {
        Subscription subscription = subscriptionRepository.findBySubscriptionName(subscriptionName);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + subscriptionName + " not found");
        }
        if (!subscription.getUsers().isEmpty()) {
            throw new IllegalArgumentException("Subscription with subscriptionName " + subscriptionName + " has users and cannot be deleted");
        }
        subscriptionRepository.deleteBySubscriptionName(subscriptionName);
    }

    /*
    When deleting all subscriptions, the ADMIN and REGISTRATION subscriptions are not deleted.
     */
    public String deleteAllSubscription() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        if (subscriptions.isEmpty()) {
            throw new NoSuchElementException("Subscriptions not found");
        }
        List<Subscription> deletableSubscriptions = subscriptions.stream()
                .filter(subscription -> !subscription.getSubscriptionName().equals("ADMIN")
                        && !subscription.getSubscriptionName().equals("REGISTRATION")
                        && subscription.getUsers().isEmpty())
                .collect(Collectors.toList());

        List<Subscription> subscriptionsWithUsers = subscriptions.stream()
                .filter(subscription -> !subscription.getUsers().isEmpty())
                .collect(Collectors.toList());

        deletableSubscriptions.forEach(subscriptionRepository::delete);

        String deletedSubscriptions = deletableSubscriptions.stream()
                .map(Subscription::getSubscriptionName)
                .collect(Collectors.joining(", "));
        String notDeleteSubscriptions = subscriptionsWithUsers.stream()
                .map(Subscription::getSubscriptionName)
                .collect(Collectors.joining(", "));

        String deletingSubscriptions = "Deleted subscriptions: " + deletedSubscriptions;
        String notDeletingSubscriptions = "Subscription with subscriptionName " + notDeleteSubscriptions + " has users and cannot be deleted";
        return deletingSubscriptions + "\n" + notDeletingSubscriptions;
    }
}
