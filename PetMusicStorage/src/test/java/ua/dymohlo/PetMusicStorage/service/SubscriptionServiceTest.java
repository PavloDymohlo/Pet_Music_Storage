package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.dymohlo.PetMusicStorage.dto.NewSubscriptionDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateSubscriptionDurationTimeDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateSubscriptionNameDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateSubscriptionPriceDTO;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.MusicFileRepository;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private MusicFileRepository musicFileRepository;

    @Test
    public void addNewSubscription_success() {
        NewSubscriptionDTO newSubscriptionDTO = NewSubscriptionDTO.builder()
                .subscriptionName("PREMIUM").build();
        when(subscriptionRepository.existsBySubscriptionNameIgnoreCase("PREMIUM")).thenReturn(false);
        Subscription newSubscription = Subscription.builder()
                .subscriptionName(newSubscriptionDTO.getSubscriptionName())
                .subscriptionPrice(newSubscriptionDTO.getSubscriptionPrice())
                .subscriptionDurationTime(newSubscriptionDTO.getSubscriptionDurationTime()).build();

        subscriptionService.addNewSubscription(newSubscriptionDTO);

        verify(subscriptionRepository).save(newSubscription);
    }

    @Test
    public void addNewSubscription_subscriptionAlreadyExists() {
        NewSubscriptionDTO newSubscriptionDTO = NewSubscriptionDTO.builder()
                .subscriptionName("PREMIUM").build();
        when(subscriptionRepository.existsBySubscriptionNameIgnoreCase("PREMIUM")).thenReturn(true);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.addNewSubscription(newSubscriptionDTO);
        });

        assertEquals("Subscription with name " + newSubscriptionDTO.getSubscriptionName() + " already exists", exception.getMessage());
    }

    @Test
    public void findSubscriptionById_success() {
        Subscription subscription = Subscription.builder()
                .id(1L).build();
        when(subscriptionRepository.findById(1L)).thenReturn(subscription);

        Subscription findSubscription = subscriptionService.findSubscriptionById(1L);

        assertNotNull(findSubscription);
    }

    @Test
    public void findSubscriptionById_notFound() {
        when(subscriptionRepository.findById(1L)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            subscriptionService.findSubscriptionById(1L);
        });

        assertEquals("Subscription with id 1 not found", exception.getMessage());
    }

    @Test
    public void findAllSubscription_success() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        when(subscriptionRepository.findAll()).thenReturn(subscriptions);

        List<Subscription> findSubscriptions = subscriptionService.findAllSubscription();

        assertNotNull(findSubscriptions);
    }

    @Test
    public void findAllSubscription_notFound() {
        List<Subscription> subscriptions = new ArrayList<>();
        when(subscriptionRepository.findAll()).thenReturn(subscriptions);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            subscriptionService.findAllSubscription();
        });

        assertEquals("Subscriptions not found", exception.getMessage());
    }

    @Test
    public void findSubscriptionsByPrice_success() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        when(subscriptionRepository.findBySubscriptionPriceBetween(BigDecimal.valueOf(0), BigDecimal.valueOf(150)))
                .thenReturn(subscriptions);

        List<Subscription> findSubscriptions = subscriptionService
                .findSubscriptionsByPrice(BigDecimal.valueOf(0), BigDecimal.valueOf(150));

        assertNotNull(findSubscriptions);
    }


    @Test
    public void findSubscriptionsByPrice_notFound() {
        List<Subscription> subscriptions = new ArrayList<>();
        when(subscriptionRepository.findBySubscriptionPriceBetween(BigDecimal.valueOf(0), BigDecimal.valueOf(150)))
                .thenReturn(subscriptions);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            subscriptionService.findSubscriptionsByPrice(BigDecimal.valueOf(0), BigDecimal.valueOf(150));
        });

        assertEquals("Subscriptions between price " + 0 + " and " + 150 + " not found", exception.getMessage());
    }

    @Test
    public void findSubscriptionBySubscriptionName_success() {
        Subscription subscription = Subscription.builder()
                .id(1L).build();
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);

        Subscription findSubscription = subscriptionService.findSubscriptionBySubscriptionName("FREE");

        assertNotNull(findSubscription);
    }

    @Test
    public void findSubscriptionBySubscriptionName_notFound() {
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            subscriptionService.findSubscriptionBySubscriptionName("FREE");
        });

        assertEquals("Subscription with subscriptionName FREE not found", exception.getMessage());
    }

    @Test
    public void updateSubscriptionName_success() {
        UpdateSubscriptionNameDTO updateSubscriptionNameDTO = UpdateSubscriptionNameDTO.builder()
                .currentSubscriptionName("FREE")
                .newSubscriptionName("OPTIMAL").build();
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);
        when(subscriptionRepository.existsBySubscriptionNameIgnoreCase("OPTIMAL")).thenReturn(false);

        subscriptionService.updateSubscriptionName(updateSubscriptionNameDTO);
        Subscription updateSubscription = Subscription.builder()
                .subscriptionName("OPTIMAL").build();

        verify(subscriptionRepository).save(updateSubscription);
    }

    @Test
    public void updateSubscriptionName_notFound() {
        UpdateSubscriptionNameDTO updateSubscriptionNameDTO = UpdateSubscriptionNameDTO.builder()
                .currentSubscriptionName("FREE")
                .newSubscriptionName("OPTIMAL").build();
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            subscriptionService.updateSubscriptionName(updateSubscriptionNameDTO);
        });

        assertEquals("Subscription with subscriptionName "
                + updateSubscriptionNameDTO.getCurrentSubscriptionName() + " not found", exception.getMessage());
    }

    @Test
    public void updateSubscriptionName_alreadyExists() {
        UpdateSubscriptionNameDTO updateSubscriptionNameDTO = UpdateSubscriptionNameDTO.builder()
                .currentSubscriptionName("FREE")
                .newSubscriptionName("OPTIMAL").build();
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);
        when(subscriptionRepository.existsBySubscriptionNameIgnoreCase("OPTIMAL")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.updateSubscriptionName(updateSubscriptionNameDTO);
        });

        assertEquals("Subscription with subscriptionName "
                + updateSubscriptionNameDTO.getNewSubscriptionName() + " already exists", exception.getMessage());
    }

    @Test
    public void updateSubscriptionPrice_success() {
        UpdateSubscriptionPriceDTO updateSubscriptionPriceDTO = UpdateSubscriptionPriceDTO.builder()
                .subscriptionName("FREE")
                .newPrice(BigDecimal.valueOf(0)).build();
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);

        subscriptionService.updateSubscriptionPrice(updateSubscriptionPriceDTO);
        Subscription updateSubscription = Subscription.builder()
                .subscriptionName("FREE")
                .subscriptionPrice(BigDecimal.valueOf(0)).build();

        verify(subscriptionRepository).save(updateSubscription);
    }

    @Test
    public void updateSubscriptionPrice_notFound() {
        UpdateSubscriptionPriceDTO updateSubscriptionPriceDTO = UpdateSubscriptionPriceDTO.builder()
                .subscriptionName("FREE")
                .newPrice(BigDecimal.valueOf(0)).build();
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            subscriptionService.updateSubscriptionPrice(updateSubscriptionPriceDTO);
        });

        assertEquals("Subscription with subscriptionName "
                + updateSubscriptionPriceDTO.getSubscriptionName() + " not found", exception.getMessage());
    }

    @Test
    public void updateSubscriptionDurationTime_success() {
        UpdateSubscriptionDurationTimeDTO updateSubscriptionDurationTimeDTO = UpdateSubscriptionDurationTimeDTO.builder()
                .subscriptionName("FREE")
                .newDurationTime(5).build();
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);

        subscriptionService.updateSubscriptionDurationTime(updateSubscriptionDurationTimeDTO);
        Subscription updateSubscription = Subscription.builder()
                .subscriptionName("FREE")
                .subscriptionDurationTime(5).build();

        verify(subscriptionRepository).save(updateSubscription);
    }

    @Test
    public void updateSubscriptionDurationTime_notFound() {
        UpdateSubscriptionDurationTimeDTO updateSubscriptionDurationTimeDTO = UpdateSubscriptionDurationTimeDTO.builder()
                .subscriptionName("FREE")
                .newDurationTime(5).build();
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            subscriptionService.updateSubscriptionDurationTime(updateSubscriptionDurationTimeDTO);
        });

        assertEquals("Subscription with subscriptionName "
                + updateSubscriptionDurationTimeDTO.getSubscriptionName() + " not found", exception.getMessage());
    }

    @Test
    public void deleteSubscriptionById_success() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        List<User> users = new ArrayList<>();
        List<MusicFile> musicFiles = new ArrayList<>();
        subscription.setMusicFiles(musicFiles);
        subscription.setUsers(users);
        when(subscriptionRepository.findById(1L)).thenReturn(subscription);

        subscriptionService.deleteSubscriptionById(1L);

        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    public void deleteSubscriptionById_notFound() {
        when(subscriptionRepository.findById(1L)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            subscriptionService.deleteSubscriptionById(1L);
        });

        assertEquals("Subscription with id " + 1 + " not found", exception.getMessage());
    }

    @Test
    public void deleteSubscriptionById_subscriptionHasUsers() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        User user = User.builder()
                .id(1L).build();
        List<User> users = new ArrayList<>();
        users.add(user);
        subscription.setUsers(users);
        when(subscriptionRepository.findById(1L)).thenReturn(subscription);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.deleteSubscriptionById(1L);
        });

        assertEquals("Subscription with id " + 1 + " has users and cannot be deleted", exception.getMessage());
    }

    @Test
    public void deleteSubscriptionBySubscriptionName_success() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        List<User> users = new ArrayList<>();
        List<MusicFile> musicFiles = new ArrayList<>();
        subscription.setMusicFiles(musicFiles);
        subscription.setUsers(users);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);

        subscriptionService.deleteSubscriptionBySubscriptionName("FREE");

        verify(subscriptionRepository).deleteBySubscriptionNameIgnoreCase("FREE");
    }

    @Test
    public void deleteSubscriptionBySubscriptionName_notFound() {
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            subscriptionService.deleteSubscriptionBySubscriptionName("FREE");
        });

        assertEquals("Subscription with subscriptionName FREE not found", exception.getMessage());
    }

    @Test
    public void deleteSubscriptionBySubscriptionName_subscriptionHasUsers() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        User user = User.builder()
                .id(1L).build();
        List<User> users = new ArrayList<>();
        users.add(user);
        subscription.setUsers(users);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.deleteSubscriptionBySubscriptionName("FREE");
        });

        assertEquals("Subscription with subscriptionName FREE has users and cannot be deleted", exception.getMessage());
    }

    @Test
    public void deleteAllSubscription_success() {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE")
                .musicFiles(new ArrayList<>())
                .users(new ArrayList<>()).build();
        Subscription adminSubscription = Subscription.builder()
                .subscriptionName("ADMIN")
                .musicFiles(new ArrayList<>())
                .users(new ArrayList<>())
                .build();
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        subscriptions.add(adminSubscription);
        when(subscriptionRepository.findAll()).thenReturn(subscriptions);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("ADMIN")).thenReturn(adminSubscription);

        subscriptionService.deleteAllSubscription();

        verify(subscriptionRepository).delete(subscription);
    }
}