package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.dymohlo.PetMusicStorage.Enum.AutoRenewStatus;
import ua.dymohlo.PetMusicStorage.controller.PaymentController;
import ua.dymohlo.PetMusicStorage.dto.*;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.MusicFileRepository;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final MusicFileRepository musicFileRepository;
    private final PaymentController paymentController;
    private final UserService userService;
    private final DatabaseUserDetailsService databaseUserDetailsService;
    private final JWTService jwtService;

    public void addNewSubscription(NewSubscriptionDTO newSubscriptionDTO) {
        if (subscriptionRepository.existsBySubscriptionNameIgnoreCase(newSubscriptionDTO.getSubscriptionName())) {
            throw new IllegalArgumentException("Subscription with name "
                    + newSubscriptionDTO.getSubscriptionName() + " already exists");
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
        Subscription subscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(subscriptionName);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + subscriptionName + " not found");
        }
        return subscription;
    }

    public void updateSubscriptionName(UpdateSubscriptionNameDTO updateSubscriptionNameDTO) {
        Subscription subscription = subscriptionRepository
                .findBySubscriptionNameIgnoreCase(updateSubscriptionNameDTO.getCurrentSubscriptionName());
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName "
                    + updateSubscriptionNameDTO.getCurrentSubscriptionName() + " not found");
        }
        if (subscriptionRepository.existsBySubscriptionNameIgnoreCase(updateSubscriptionNameDTO.getNewSubscriptionName())) {
            throw new IllegalArgumentException("Subscription with subscriptionName "
                    + updateSubscriptionNameDTO.getNewSubscriptionName() + " already exists");
        }
        subscription.setSubscriptionName(updateSubscriptionNameDTO.getNewSubscriptionName());
        subscriptionRepository.save(subscription);
    }

    public void updateSubscriptionPrice(UpdateSubscriptionPriceDTO updateSubscriptionPriceDTO) {
        Subscription subscription = subscriptionRepository
                .findBySubscriptionNameIgnoreCase(updateSubscriptionPriceDTO.getSubscriptionName());
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName "
                    + updateSubscriptionPriceDTO.getSubscriptionName() + " not found");
        }
        subscription.setSubscriptionPrice(updateSubscriptionPriceDTO.getNewPrice());
        subscriptionRepository.save(subscription);
    }

    public void updateSubscriptionDurationTime(UpdateSubscriptionDurationTimeDTO updateSubscriptionDurationTimeDTO) {
        Subscription subscription = subscriptionRepository
                .findBySubscriptionNameIgnoreCase(updateSubscriptionDurationTimeDTO.getSubscriptionName());
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName "
                    + updateSubscriptionDurationTimeDTO.getSubscriptionName() + " not found");
        }
        subscription.setSubscriptionDurationTime(updateSubscriptionDurationTimeDTO.getNewDurationTime());
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void deleteSubscriptionById(long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with id " + subscriptionId + " not found");
        }
        if (!subscription.getUsers().isEmpty()) {
            throw new IllegalArgumentException("Subscription with id " + subscriptionId + " has users and cannot be deleted");
        }
        if (!subscription.getMusicFiles().isEmpty()) {
            transferMusicFiles(subscription);
        }
        subscriptionRepository.delete(subscription);
    }

    @Transactional
    private void transferMusicFiles(Subscription subscription) {
        Subscription newSubscription = subscriptionRepository.findBySubscriptionNameIgnoreCase("ADMIN");
        if (newSubscription == null) {
            throw new NoSuchElementException("Subscription with name 'ADMIN' not found");
        }
        List<MusicFile> musicFiles = subscription.getMusicFiles();
        musicFiles.stream()
                .peek(musicFile -> musicFile.setSubscription(newSubscription))
                .forEach(musicFileRepository::save);
        subscription.getMusicFiles().clear();
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void deleteSubscriptionBySubscriptionName(String subscriptionName) {
        Subscription subscription = subscriptionRepository.findBySubscriptionNameIgnoreCase(subscriptionName);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + subscriptionName + " not found");
        }
        if (!subscription.getUsers().isEmpty()) {
            throw new IllegalArgumentException("Subscription with subscriptionName "
                    + subscriptionName + " has users and cannot be deleted");
        }
        if (!subscription.getMusicFiles().isEmpty()) {
            transferMusicFiles(subscription);
        }
        subscriptionRepository.deleteBySubscriptionNameIgnoreCase(subscriptionName);
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
        deletableSubscriptions.forEach(subscription -> {
            transferMusicFiles(subscription);
            subscriptionRepository.delete(subscription);
        });
        String deletedSubscriptions = deletableSubscriptions.stream()
                .map(Subscription::getSubscriptionName)
                .collect(Collectors.joining(", "));
        String notDeleteSubscriptions = subscriptionsWithUsers.stream()
                .map(Subscription::getSubscriptionName)
                .collect(Collectors.joining(", "));
        String deletingSubscriptions = "Deleted subscriptions: " + deletedSubscriptions;
        String notDeletingSubscriptions = "Subscription with subscriptionName "
                + notDeleteSubscriptions + " has users and cannot be deleted";
        return deletingSubscriptions + "\n" + notDeletingSubscriptions;
    }

    private void handleUpdateSubscription(User user) {
        Subscription subscription = subscriptionRepository
                .findBySubscriptionNameIgnoreCase(user.getSubscription().getSubscriptionName());
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .outputCardNumber(user.getUserBankCard().getCardNumber())
                .sum(subscription.getSubscriptionPrice())
                .cardExpirationDate(user.getUserBankCard().getCardExpirationDate())
                .cvv(user.getUserBankCard().getCvv()).build();
        ResponseEntity<String> paymentResponse = paymentController.payment(transactionDTO);
        if (paymentResponse.getStatusCode().is2xxSuccessful()) {
            UpdateSubscriptionDTO updateSubscriptionDTO = UpdateSubscriptionDTO.builder()
                    .newSubscription(subscription).build();
            userService.updateSubscription(user.getPhoneNumber(), updateSubscriptionDTO);
        } else if (paymentResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
            handleFreeSubscription(user);
        }
    }

    private void handleFreeSubscription(User user) {
        userService.setFreeSubscription(user.getPhoneNumber());
        UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(user.getPhoneNumber()));
        jwtService.generateJwtToken(userDetails);
    }

    public void autoRenewSubscriptionForUser(User user) {
        try {
            if (user.getAutoRenew().equals(AutoRenewStatus.YES)) {
                handleUpdateSubscription(user);
            } else {
                handleFreeSubscription(user);
            }
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
        }
    }
}