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
import ua.dymohlo.PetMusicStorage.dto.*;
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
    @Mock
    private Subscription mockSubscription;
    @Mock
    private JWTService mockJwtService;
    @Mock
    private UserBankCardService mockUserBankCardService;

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

            assertEquals("Phone number " + phoneNumber + " already exists", e.getMessage());
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

            assertEquals("Email " + email + " already exists", e.getMessage());
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

    @Test
    public void loginIn_successful_returnSuccess() {
        UserLoginInDTO userLoginInDTO = UserLoginInDTO.builder()
                .phoneNumber(80966584100L)
                .password("password").build();
        mockUser = User.builder()
                .phoneNumber(80966584100L)
                .password("password").build();
        when(mockUserRepository.existsByPhoneNumber(anyLong())).thenReturn(true);
        when(mockUserRepository.findByPhoneNumber(80966584100L)).thenReturn(mockUser);
        when(mockPasswordEncoder.matches("password", "password")).thenReturn(true);

        String result = userService.loginIn(userLoginInDTO);

        assertEquals("Success", result);
        verify(mockUserRepository, times(1)).findByPhoneNumber(80966584100L);
        verify(mockPasswordEncoder, times(1)).matches("password", "password");
    }

    @Test
    public void loginIn_invalidPhoneNumber_returnIllegalArgumentException() {
        UserLoginInDTO userLoginInDTO = UserLoginInDTO.builder()
                .phoneNumber(80966584100L).build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginIn(userLoginInDTO);
        });

        assert exception.getMessage().equals("Phone number " + userLoginInDTO.getPhoneNumber() + " not found");
    }

    @Test
    public void loginIn_invalidPassword_returnIllegalArgumentException() {
        UserLoginInDTO userLoginInDTO = UserLoginInDTO.builder()
                .phoneNumber(80966584100L)
                .password("password").build();
        mockUser = User.builder()
                .phoneNumber(80966584100L)
                .password("1234").build();
        when(mockUserRepository.existsByPhoneNumber(anyLong())).thenReturn(true);
        when(mockUserRepository.findByPhoneNumber(anyLong())).thenReturn(mockUser);
        when(mockPasswordEncoder.matches("password", "1234")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginIn(userLoginInDTO);
        });

        assert exception.getMessage().equals("Incorrect password for user with phone number " + userLoginInDTO.getPhoneNumber());
    }

    @Test
    public void isAdminSubscription_returnTrue() {
        mockSubscription = Subscription.builder()
                .subscriptionName("ADMIN").build();
        mockUser = User.builder()
                .subscription(mockSubscription).build();
        when(mockSubscriptionRepository.findBySubscriptionName("ADMIN")).thenReturn(mockSubscription);
        Subscription subscription = mockUser.getSubscription();

        boolean result = subscription == mockSubscriptionRepository.findBySubscriptionName("ADMIN");

        assertTrue(result);
        verify(mockSubscriptionRepository, times(1)).findBySubscriptionName("ADMIN");
    }

    @Test
    public void isAdminSubscription_returnFalse() {
        mockSubscription = Subscription.builder()
                .subscriptionName("FREE").build();
        mockUser = User.builder()
                .subscription(mockSubscription).build();
        when(mockSubscriptionRepository.findBySubscriptionName("ADMIN")).thenReturn(null);
        Subscription subscription = mockUser.getSubscription();

        boolean result = subscription == mockSubscriptionRepository.findBySubscriptionName("ADMIN");

        assertFalse(result);
        verify(mockSubscriptionRepository, times(1)).findBySubscriptionName("ADMIN");
    }

    @Test
    public void getCurrentUserPhoneNumber_returnPhoneNumber() {
        String jwtToken = "Bearer <token>";
        long phoneNumber = 80996658896L;
        when(mockJwtService.extractUserName(anyString())).thenReturn(String.valueOf(phoneNumber));

        long actualPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);

        verify(mockJwtService).extractUserName(anyString());
        assertEquals(Long.parseLong(String.valueOf(phoneNumber)), actualPhoneNumber);
    }

    @Test
    public void updatePhoneNumber_phoneNumberNotFound() {
        long currentPhoneNumber = 80998885566L;
        long newPhoneNumber = 80663210022L;
        when(mockUserRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePhoneNumber(currentPhoneNumber, newPhoneNumber);
        });

        assert exception.getMessage().equals("Phone number " + currentPhoneNumber + " not found");
    }

    @Test
    public void updatePhoneNumber_newPhoneNumberAlreadyExists() {
        long currentPhoneNumber = 80998885566L;
        long newPhoneNumber = 80663210022L;
        when(mockUserRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(true);
        when(mockUserRepository.existsByPhoneNumber(newPhoneNumber)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePhoneNumber(currentPhoneNumber, newPhoneNumber);
        });

        assert exception.getMessage().equals("Phone number " + newPhoneNumber + " already exists");
    }

    @Test
    public void updatePhoneNumber_success() {
        long currentPhoneNumber = 80998866555L;
        long newPhoneNumber = 80985623300L;
        mockUser = User.builder()
                .phoneNumber(80998866555L).build();
        when(mockUserRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(true);
        when(mockUserRepository.existsByPhoneNumber(newPhoneNumber)).thenReturn(false);
        when(mockUserRepository.findByPhoneNumber(currentPhoneNumber)).thenReturn(mockUser);

        userService.updatePhoneNumber(currentPhoneNumber, newPhoneNumber);

        assertEquals(newPhoneNumber, mockUser.getPhoneNumber());
        verify(mockUserRepository).save(mockUser);
    }

    @Test
    public void updateBankCard_phoneNumberNotFound() {
        long currentPhoneNumber = 80998885566L;
        UpdateUserBankCardDTO mockUpdateUserBankCardDTO = UpdateUserBankCardDTO.builder()
                .userPhoneNumber(currentPhoneNumber)
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(1234567890125874L)
                        .cardExpirationDate("25/25")
                        .cvv((short) 123).build()).build();

        when(mockUserRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateBankCard(currentPhoneNumber, mockUpdateUserBankCardDTO);
        });

        assert exception.getMessage().equals("Phone number " + currentPhoneNumber + " not found");
    }

    @Test
    public void updateBankCard_invalidCardDetails() {
        long currentPhoneNumber = 80998885566L;
        UpdateUserBankCardDTO mockUpdateUserBankCardDTO = UpdateUserBankCardDTO.builder()
                .userPhoneNumber(currentPhoneNumber)
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(1234567890125874L)
                        .cardExpirationDate("25/25")
                        .cvv((short) 123).build()).build();
        when(mockUserRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(true);
        when(mockUserBankCardRepository.findByCardNumber(mockUpdateUserBankCardDTO.getNewUserBankCard().getCardNumber()))
                .thenReturn(mockUserBankCard);
        when(mockUserBankCardService.validateBankCard(mockUpdateUserBankCardDTO.getNewUserBankCard())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateBankCard(currentPhoneNumber, mockUpdateUserBankCardDTO);
        });

        assert exception.getMessage().equals("Invalid card details for user with phone number " + currentPhoneNumber);
    }

    @Test
    public void updateBankCard_cardExists_updateCard() {
        long currentPhoneNumber = 80998885566L;
        long newCardNumber = 9876543210987654L;
        mockUser = new User();
        UpdateUserBankCardDTO mockUpdateUserBankCardDTO = UpdateUserBankCardDTO.builder()
                .userPhoneNumber(currentPhoneNumber)
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(newCardNumber)
                        .cardExpirationDate("25/25")
                        .cvv((short) 123)
                        .build())
                .build();
        UserBankCard existingCard = UserBankCard.builder()
                .cardNumber(mockUpdateUserBankCardDTO.getNewUserBankCard().getCardNumber())
                .cardExpirationDate(mockUpdateUserBankCardDTO.getNewUserBankCard().getCardExpirationDate())
                .cvv(mockUpdateUserBankCardDTO.getNewUserBankCard().getCvv())
                .build();
        when(mockUserRepository.findByPhoneNumber(currentPhoneNumber)).thenReturn(mockUser);
        when(mockUserRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(true);
        when(mockUserBankCardRepository.findByCardNumber(mockUpdateUserBankCardDTO.getNewUserBankCard().getCardNumber()))
                .thenReturn(existingCard);
        when(mockUserBankCardService.validateBankCard(existingCard)).thenReturn(true);

        userService.updateBankCard(currentPhoneNumber, mockUpdateUserBankCardDTO);

        verify(mockUserRepository).existsByPhoneNumber(currentPhoneNumber);
        verify(mockUserBankCardRepository).findByCardNumber(newCardNumber);
        verify(mockUserBankCardService).validateBankCard(existingCard);
        verify(mockUserRepository).save(any(User.class));
    }

    @Test
    public void updateBankCard_newBankCard() {
        long currentPhoneNumber = 80998885566L;
        long newCardNumber = 9876543210987654L;
        mockUser = new User();
        UpdateUserBankCardDTO mockUpdateUserBankCardDTO = UpdateUserBankCardDTO.builder()
                .userPhoneNumber(currentPhoneNumber)
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(newCardNumber)
                        .cardExpirationDate("25/25")
                        .cvv((short) 123)
                        .build())
                .build();
        when(mockUserRepository.findByPhoneNumber(currentPhoneNumber)).thenReturn(mockUser);
        when(mockUserRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(true);
        when(mockUserBankCardRepository.findByCardNumber(mockUpdateUserBankCardDTO.getNewUserBankCard().getCardNumber()))
                .thenReturn(null);

        userService.updateBankCard(currentPhoneNumber, mockUpdateUserBankCardDTO);

        verify(mockUserRepository).existsByPhoneNumber(currentPhoneNumber);
        verify(mockUserBankCardRepository).findByCardNumber(newCardNumber);
        verify(mockUserRepository).save(any(User.class));
    }

    @Test
    public void updatePassword_success() {
        long userPhoneNumber = 80998885566L;
        String newPassword = "password";
        UpdatePasswordDTO updatePasswordDTO = UpdatePasswordDTO.builder()
                .newPassword(newPassword).build();
        when(mockUserRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(true);
        when(mockUserRepository.findByPhoneNumber(userPhoneNumber)).thenReturn(mockUser);
        when(mockPasswordEncoder.encode(newPassword)).thenReturn(newPassword);

        userService.updatePassword(userPhoneNumber, updatePasswordDTO);

        verify(mockUserRepository).save(any(User.class));
    }

    @Test
    public void updatePassword_phoneNumberNotFound() {
        long userPhoneNumber = 80998885566L;
        String newPassword = "password";
        UpdatePasswordDTO updatePasswordDTO = UpdatePasswordDTO.builder()
                .newPassword(newPassword)
                .userPhoneNumber(userPhoneNumber).build();
        when(mockUserRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(false);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePassword(userPhoneNumber, updatePasswordDTO);
        });

        assert exception.getMessage().equals("Phone number " + userPhoneNumber + " not found");
    }

    @Test
    public void updateEmail_success() {
        long userPhoneNumber = 80998885566L;
        String newEmail = "newEmail@example.com";
        UpdateEmailDTO updateEmailDTO = UpdateEmailDTO.builder()
                .userPhoneNumber(userPhoneNumber)
                .newEmail(newEmail).build();
        when(mockUserRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(true);
        when(mockUserRepository.existsByEmail(newEmail)).thenReturn(false);
        when(mockUserRepository.findByPhoneNumber(userPhoneNumber)).thenReturn(mockUser);

        userService.updateEmail(userPhoneNumber, updateEmailDTO);

        assertEquals(newEmail, updateEmailDTO.getNewEmail());
        verify(mockUserRepository).save(any(User.class));
    }

    @Test
    public void updateEmail_emailIsAlreadyExists() {
        long userPhoneNumber = 80998885566L;
        String newEmail = "newEmail@example.com";
        UpdateEmailDTO updateEmailDTO = UpdateEmailDTO.builder()
                .userPhoneNumber(userPhoneNumber)
                .newEmail(newEmail).build();
        when(mockUserRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(true);
        when(mockUserRepository.existsByEmail(newEmail)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateEmail(userPhoneNumber, updateEmailDTO);
        });

        assert exception.getMessage().equals("Email " + updateEmailDTO.getNewEmail() + " is already exists");
    }

    @Test
    public void setAutoRenewStatus_success() {
        long phoneNumber = 80998885566L;
        AutoRenewStatus newStatus = AutoRenewStatus.YES;
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);
        when(mockUserRepository.findByPhoneNumber(phoneNumber)).thenReturn(mockUser);

        userService.setAutoRenewStatus(phoneNumber, new SetAutoRenewDTO(phoneNumber, newStatus));

        verify(mockUser, times(1)).setAutoRenew(newStatus);
        verify(mockUserRepository, times(1)).save(mockUser);
    }

    @Test
    public void updateSubscription_success() {
        long phoneNumber = 80998885566L;
        Subscription newSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        UpdateSubscriptionDTO updateSubscriptionDTO = UpdateSubscriptionDTO.builder()
                .newSubscription(newSubscription).build();
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);
        when(mockUserRepository.findByPhoneNumber(phoneNumber)).thenReturn(mockUser);
        when(mockSubscriptionRepository.findBySubscriptionName(anyString())).thenReturn(newSubscription);

        userService.updateSubscription(phoneNumber, updateSubscriptionDTO);

        verify(mockUser, times(1)).setSubscription(newSubscription);
        verify(mockUserRepository, times(1)).save(mockUser);
    }

    @Test
    public void updateSubscription_subscriptionNotFound() {
        long phoneNumber = 80998885566L;
        Subscription newSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        UpdateSubscriptionDTO updateSubscriptionDTO = UpdateSubscriptionDTO.builder()
                .newSubscription(newSubscription).build();
        when(mockUserRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);
        when(mockSubscriptionRepository.findBySubscriptionName(anyString())).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateSubscription(phoneNumber, updateSubscriptionDTO);
        });

        assert exception.getMessage().equals("Subscription with name " + newSubscription.getSubscriptionName() + " not found");
    }
}