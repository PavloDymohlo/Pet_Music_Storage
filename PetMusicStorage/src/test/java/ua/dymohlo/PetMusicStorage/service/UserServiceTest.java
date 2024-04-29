package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.dymohlo.PetMusicStorage.Enum.AutoRenewStatus;
import ua.dymohlo.PetMusicStorage.dto.UserRegistrationDTO;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.repository.UserBankCardRepository;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private DatabaseUserDetailsService mockDatabaseUserDetailsService;
    @Mock
    private UserBankCard mockUserBankCard;
    @Mock
    private UserBankCardRepository mockUserBankCardRepository;
    @Mock
    private SubscriptionRepository mockSubscriptionRepository;
    @Mock
    private User mockUser;
    @Mock
    private PasswordEncoder mockPasswordEncoder;

    @Test
    public void userDetailsService_phoneNumberExist() {
        long phoneNumber = 80996663322L;
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockDatabaseUserDetailsService.loadUserByUsername(String.valueOf(phoneNumber))).thenReturn(mockUserDetails);

        UserDetailsService userDetailsService = userService.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(phoneNumber));

        assertNotNull(userDetails);
        assertEquals(mockUserDetails, userDetails);
        verify(mockDatabaseUserDetailsService).loadUserByUsername(String.valueOf(phoneNumber));
    }

    @Test
    public void userDetailService_phoneNumberNotFound() {
        long phoneNumber = 80996663322L;

        UserDetailsService userDetailsService = userService.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(phoneNumber));

        assertNull(userDetails);
    }

    @Test
    public void registerUser_success() {
        long phoneNumber = 80996663322L;
        String email = "testuser@example.com";
        String password = mockPasswordEncoder.encode("password");
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);
        when(mockUserRepository.existsByEmail(email)).thenReturn(false);
        mockUserBankCard = UserBankCard.builder().cardNumber(1234567890123456L).build();
        when(mockUserBankCardRepository.findByCardNumber(anyLong())).thenReturn(mockUserBankCard);
        Subscription firstSubscription = Subscription.builder().subscriptionName("MAXIMUM").build();
        when(mockSubscriptionRepository.findBySubscriptionName("MAXIMUM")).thenReturn(firstSubscription);
        when(mockSubscriptionRepository.existsBySubscriptionName(String.valueOf(firstSubscription))).thenReturn(true);
        UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
                .phoneNumber(phoneNumber)
                .userBankCard(mockUserBankCard)
                .password(password)
                .email(email).build();

        userService.registerUser(userRegistrationDTO);

        verify(mockUserRepository).save(any(User.class));
        verify(mockUserRepository, times(1)).save(any(User.class));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser);
        assertEquals(phoneNumber, savedUser.getPhoneNumber());
        assertEquals(email, savedUser.getEmail());
        assertEquals(firstSubscription, savedUser.getSubscription());
        assertEquals(mockUserBankCard, savedUser.getUserBankCard());
    }

    @Test
    public void registerUser_phoneNumberExists_registrationFailed() {
        long phoneNumber = 80996663322L;
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);
        UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
                .phoneNumber(phoneNumber).build();

        try {
            userService.registerUser(userRegistrationDTO);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {

            assertEquals("Phone number already exists", e.getMessage());
            verify(mockUserRepository, never()).save(any(User.class));
        }
    }

    @Test
    public void registerUser_emailNumberExists_registrationFailed() {
        long phoneNumber = 80996663322L;
        String email = "testuser@example.com";
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);
        when(mockUserRepository.existsByEmail(email)).thenReturn(true);
        UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
                .phoneNumber(phoneNumber)
                .email(email).build();

        try {
            userService.registerUser(userRegistrationDTO);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {

            assertEquals("Email already exists", e.getMessage());
            verify(mockUserRepository, never()).save(any(User.class));
        }
    }

    @Test
    public void isPhoneNumberRegistered_phoneNumberExists_returnTrue() {
        long phoneNumber = 80996663322L;
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        boolean result = userService.isPhoneNumberRegistered(phoneNumber);

        assertTrue(result);
        verify(mockUserRepository).existsByPhoneNumber(phoneNumber);
    }

    @Test
    public void isPhoneNumberRegistered_phoneNumberNotExists_returnFalse() {
        long phoneNumber = 80996663322L;
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);

        boolean result = userService.isPhoneNumberRegistered(phoneNumber);

        assertFalse(result);
        verify(mockUserRepository).existsByPhoneNumber(phoneNumber);
    }

    @Test
    public void isEmailRegistered_emailExists_returnTrue() {
        String email = "testuser@example.com";
        when(mockUserRepository.existsByEmail(email)).thenReturn(true);

        boolean result = userService.isEmailRegistered(email);

        assertTrue(result);
        verify(mockUserRepository).existsByEmail(email);
    }

    @Test
    public void isEmailRegistered_emailNotExists_returnFalse() {
        String email = "testuser@example.com";
        when(mockUserRepository.existsByEmail(email)).thenReturn(false);

        boolean result = userService.isEmailRegistered(email);

        assertFalse(result);
        verify(mockUserRepository).existsByEmail(email);
    }
}