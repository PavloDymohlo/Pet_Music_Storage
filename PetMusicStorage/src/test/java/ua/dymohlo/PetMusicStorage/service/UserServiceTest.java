package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DatabaseUserDetailsService databaseUserDetailsService;
    @Mock
    private UserBankCard userBankCard;
    @Mock
    private UserBankCardRepository userBankCardRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private User user;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Subscription subscription;
    @Mock
    private JWTService jwtService;
    @Mock
    private UserBankCardService userBankCardService;
    @Mock
    private TelegramService telegramService;
    @Mock
    private EmailService emailService;

    @Test
    public void userDetailsService_phoneNumberExist() {
        long phoneNumber = 80996663322L;
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(databaseUserDetailsService.loadUserByUsername(String.valueOf(phoneNumber))).thenReturn(mockUserDetails);

        UserDetailsService userDetailsService = userService.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(phoneNumber));

        assertNotNull(userDetails);
        assertEquals(mockUserDetails, userDetails);
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
        String password = passwordEncoder.encode("password");
        userBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L).build();
        Subscription firstSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
                .phoneNumber(phoneNumber)
                .userBankCard(userBankCard)
                .password(password)
                .email(email).build();
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(false);
        when(userBankCardRepository.findByCardNumber(anyLong())).thenReturn(userBankCard);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("MAXIMUM")).thenReturn(firstSubscription);
        when(subscriptionRepository.existsBySubscriptionNameIgnoreCase(String.valueOf(firstSubscription))).thenReturn(true);

        userService.registerUser(userRegistrationDTO);

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void registerUser_phoneNumberAlreadyExists() {
        long phoneNumber = 80996663322L;
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);
        UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
                .phoneNumber(phoneNumber).build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(userRegistrationDTO);
        });

        assertEquals("Phone number " + phoneNumber + " already exists", exception.getMessage());
    }

    @Test
    public void registerUser_emailAlreadtExists() {
        long phoneNumber = 80996663322L;
        String email = "testuser@example.com";
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(true);
        UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
                .phoneNumber(phoneNumber)
                .email(email).build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(userRegistrationDTO);
        });

        assertEquals("Email " + email + " already exists", exception.getMessage());
    }

    @Test
    public void isPhoneNumberRegistered_true() {
        long phoneNumber = 80996663322L;
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        boolean result = userService.isPhoneNumberRegistered(phoneNumber);

        assertTrue(result);
    }

    @Test
    public void isPhoneNumberRegistered_false() {
        long phoneNumber = 80996663322L;
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);

        boolean result = userService.isPhoneNumberRegistered(phoneNumber);

        assertFalse(result);
        verify(userRepository).existsByPhoneNumber(phoneNumber);
    }

    @Test
    public void isEmailRegistered_true() {
        String email = "testuser@example.com";
        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(true);

        boolean result = userService.isEmailRegistered(email);

        assertTrue(result);
    }

    @Test
    public void isEmailRegistered_false() {
        String email = "testuser@example.com";
        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(false);

        boolean result = userService.isEmailRegistered(email);

        assertFalse(result);
    }

    @Test
    public void loginIn_success() {
        UserLoginInDTO userLoginInDTO = UserLoginInDTO.builder()
                .phoneNumber(80966584100L)
                .password("password").build();
        user = User.builder()
                .phoneNumber(80966584100L)
                .password("password").build();
        when(userRepository.existsByPhoneNumber(anyLong())).thenReturn(true);
        when(userRepository.findByPhoneNumber(80966584100L)).thenReturn(user);
        when(passwordEncoder.matches("password", "password")).thenReturn(true);

        String result = userService.loginIn(userLoginInDTO);

        assertEquals("Success", result);
    }

    @Test
    public void loginIn_invalidPhoneNumber() {
        UserLoginInDTO userLoginInDTO = UserLoginInDTO.builder()
                .phoneNumber(80966584100L).build();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.loginIn(userLoginInDTO);
        });

        assert exception.getMessage().equals("Phone number " + userLoginInDTO.getPhoneNumber() + " not found");
    }

    @Test
    public void loginIn_invalidPassword() {
        UserLoginInDTO userLoginInDTO = UserLoginInDTO.builder()
                .phoneNumber(80966584100L)
                .password("password").build();
        user = User.builder()
                .phoneNumber(80966584100L)
                .password("1234").build();
        when(userRepository.existsByPhoneNumber(anyLong())).thenReturn(true);
        when(userRepository.findByPhoneNumber(anyLong())).thenReturn(user);
        when(passwordEncoder.matches("password", "1234")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginIn(userLoginInDTO);
        });

        assert exception.getMessage().equals("Incorrect password for user with phone number " + userLoginInDTO.getPhoneNumber());
    }

    @Test
    public void isAdminSubscription_true() {
        subscription = Subscription.builder()
                .subscriptionName("ADMIN").build();
        user = User.builder()
                .subscription(subscription)
                .phoneNumber(80663201200L).build();
        when(userRepository.findByPhoneNumber(80663201200L)).thenReturn(user);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("ADMIN")).thenReturn(subscription);

        boolean userSubscription = userService.isAdminSubscription(80663201200L);

        assertTrue(userSubscription);
    }

    @Test
    public void isAdminSubscription_false() {
        subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        user = User.builder()
                .subscription(subscription)
                .phoneNumber(80663201200L).build();
        when(userRepository.findByPhoneNumber(80663201200L)).thenReturn(user);

        boolean userSubscription = userService.isAdminSubscription(80663201200L);

        assertFalse(userSubscription);
    }

    @Test
    public void updatePhoneNumber_success() {
        long currentPhoneNumber = 80998866555L;
        long newPhoneNumber = 80985623300L;
        user = User.builder()
                .phoneNumber(80998866555L)
                .build();
        when(userRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(true);
        when(userRepository.existsByPhoneNumber(newPhoneNumber)).thenReturn(false);
        when(userRepository.findByPhoneNumber(currentPhoneNumber)).thenReturn(user);
        doNothing().when(telegramService).notifyUserAboutChangePhoneNumber(newPhoneNumber);
        doNothing().when(emailService).notifyUserAboutChangePhoneNumber(newPhoneNumber, user.getEmail());

        userService.updatePhoneNumber(currentPhoneNumber, newPhoneNumber);

        verify(userRepository).save(user);
    }

    @Test
    public void updatePhoneNumber_phoneNumberNotFound() {
        long currentPhoneNumber = 80998885566L;
        long newPhoneNumber = 80663210022L;
        when(userRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.updatePhoneNumber(currentPhoneNumber, newPhoneNumber);
        });

        assert exception.getMessage().equals("Phone number " + currentPhoneNumber + " not found");
    }

    @Test
    public void updatePhoneNumber_phoneNumberAlreadyExists() {
        long currentPhoneNumber = 80998885566L;
        long newPhoneNumber = 80663210022L;
        when(userRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(true);
        when(userRepository.existsByPhoneNumber(newPhoneNumber)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePhoneNumber(currentPhoneNumber, newPhoneNumber);
        });

        assert exception.getMessage().equals("Phone number " + newPhoneNumber + " already exists");
    }

    @Test
    public void getCurrentUserPhoneNumber_success() {
        String jwtToken = "Bearer <token>";
        long phoneNumber = 80996658896L;
        when(jwtService.extractUserName(anyString())).thenReturn(String.valueOf(phoneNumber));

        userService.getCurrentUserPhoneNumber(jwtToken);

        verify(jwtService).extractUserName(anyString());
    }

    @Test
    public void updateBankCard_success() {
        long userPhoneNumber = 80998885566L;
        UserBankCard oldUserBankCard = UserBankCard.builder()
                .cardNumber(7896541236985425L)
                .cardExpirationDate("12/25")
                .cvv((short) 111)
                .users(new ArrayList<>())
                .build();
        User simpleUser = User.builder()
                .phoneNumber(userPhoneNumber)
                .userBankCard(oldUserBankCard)
                .email("user@example.com")
                .build();
        oldUserBankCard.getUsers().add(simpleUser);
        UserBankCard newUserBankCard = UserBankCard.builder()
                .cardNumber(1258986587896523L)
                .cardExpirationDate("12/25")
                .cvv((short) 111)
                .users(new ArrayList<>())
                .build();
        UpdateUserBankCardDTO updateUserBankCardDTO = UpdateUserBankCardDTO.builder()
                .userPhoneNumber(userPhoneNumber)
                .newUserBankCard(newUserBankCard)
                .build();
        when(userRepository.findByPhoneNumber(userPhoneNumber)).thenReturn(simpleUser);
        when(userRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(true);
        when(userBankCardRepository.findByCardNumber(updateUserBankCardDTO.getNewUserBankCard().getCardNumber()))
                .thenReturn(null);
        when(userBankCardRepository.findByCardNumber(oldUserBankCard.getCardNumber()))
                .thenReturn(oldUserBankCard);
        doNothing().when(telegramService).notifyUserAboutChangeBankCard(anyLong(), anyString());
        doNothing().when(emailService).notifyUserAboutChangeBankCard(simpleUser.getEmail(), newUserBankCard.getCardNumber());

        userService.updateBankCard(userPhoneNumber, updateUserBankCardDTO);

        verify(userRepository).save(simpleUser);
    }


    @Test
    public void updateBankCard_phoneNumberNotFound() {
        long currentPhoneNumber = 80998885566L;
        UpdateUserBankCardDTO mockUpdateUserBankCardDTO = UpdateUserBankCardDTO.builder()
                .userPhoneNumber(currentPhoneNumber)
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(1234567890125874L)
                        .cardExpirationDate("12/25")
                        .cvv((short) 111).build()).build();

        when(userRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.updateBankCard(currentPhoneNumber, mockUpdateUserBankCardDTO);
        });

        assertEquals("Phone number " + currentPhoneNumber + " not found", exception.getMessage());
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
        when(userRepository.existsByPhoneNumber(currentPhoneNumber)).thenReturn(true);
        when(userBankCardRepository.findByCardNumber(mockUpdateUserBankCardDTO.getNewUserBankCard().getCardNumber()))
                .thenReturn(userBankCard);
        when(userBankCardService.validateBankCard(mockUpdateUserBankCardDTO.getNewUserBankCard())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateBankCard(currentPhoneNumber, mockUpdateUserBankCardDTO);
        });

        assertEquals("Invalid card details for user with phone number " + currentPhoneNumber, exception.getMessage());
    }

    @Test
    public void updatePassword_success() {
        long userPhoneNumber = 80998885566L;
        String newPassword = "password";
        UpdatePasswordDTO updatePasswordDTO = UpdatePasswordDTO.builder()
                .currentPassword("oldPassword")
                .newPassword(newPassword).build();
        when(userRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(true);
        when(userRepository.findByPhoneNumber(userPhoneNumber)).thenReturn(user);
        when(passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newPassword);

        userService.updatePassword(userPhoneNumber, updatePasswordDTO);

        verify(userRepository).save(user);
    }

    @Test
    public void updatePassword_phoneNumberNotFound() {
        long userPhoneNumber = 80998885566L;
        String newPassword = "password";
        UpdatePasswordDTO updatePasswordDTO = UpdatePasswordDTO.builder()
                .newPassword(newPassword)
                .userPhoneNumber(userPhoneNumber).build();
        when(userRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(false);
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.updatePassword(userPhoneNumber, updatePasswordDTO);
        });

        assertEquals("Phone number " + userPhoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void updatePassword_currentPasswordIsNotCorrect() {
        long userPhoneNumber = 80998885566L;
        String newPassword = "password";
        UpdatePasswordDTO updatePasswordDTO = UpdatePasswordDTO.builder()
                .newPassword(newPassword)
                .userPhoneNumber(userPhoneNumber).build();
        when(userRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(true);
        when(userRepository.findByPhoneNumber(userPhoneNumber)).thenReturn(user);
        when(passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), user.getPassword())).thenReturn(false);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePassword(userPhoneNumber, updatePasswordDTO);
        });

        assertEquals("Current password is incorrect!", exception.getMessage());
    }

    @Test
    public void updateEmail_success() {
        long userPhoneNumber = 80998885566L;
        String newEmail = "newEmail@example.com";
        UpdateEmailDTO updateEmailDTO = UpdateEmailDTO.builder()
                .userPhoneNumber(userPhoneNumber)
                .newEmail(newEmail).build();
        when(userRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(true);
        when(userRepository.existsByEmailIgnoreCase(newEmail)).thenReturn(false);
        when(userRepository.findByPhoneNumber(userPhoneNumber)).thenReturn(user);

        userService.updateEmail(userPhoneNumber, updateEmailDTO);

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void updateEmail_phoneNumberNotFound() {
        long userPhoneNumber = 80998885566L;
        String newEmail = "newEmail@example.com";
        UpdateEmailDTO updateEmailDTO = UpdateEmailDTO.builder()
                .userPhoneNumber(userPhoneNumber)
                .newEmail(newEmail).build();
        when(userRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.updateEmail(userPhoneNumber, updateEmailDTO);
        });

        assertEquals("Phone number " + userPhoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void updateEmail_emailIsAlreadyExists() {
        long userPhoneNumber = 80998885566L;
        String newEmail = "newEmail@example.com";
        UpdateEmailDTO updateEmailDTO = UpdateEmailDTO.builder()
                .userPhoneNumber(userPhoneNumber)
                .newEmail(newEmail).build();
        when(userRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(true);
        when(userRepository.existsByEmailIgnoreCase(newEmail)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateEmail(userPhoneNumber, updateEmailDTO);
        });

        assertEquals("Email " + updateEmailDTO.getNewEmail() + " is already exists", exception.getMessage());
    }

    @Test
    public void setAutoRenewStatus_success() {
        long phoneNumber = 80998885566L;
        AutoRenewStatus newStatus = AutoRenewStatus.YES;
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(user);

        userService.setAutoRenewStatus(phoneNumber, new SetAutoRenewDTO(phoneNumber, newStatus));

        verify(user).setAutoRenew(newStatus);
    }

    @Test
    public void setAutoRenewStatus_phoneNumberNotFound() {
        long userPhoneNumber = 80998885566L;
        AutoRenewStatus newStatus = AutoRenewStatus.YES;
        when(userRepository.existsByPhoneNumber(userPhoneNumber)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.setAutoRenewStatus(userPhoneNumber, new SetAutoRenewDTO(userPhoneNumber, newStatus));
        });

        assertEquals("Phone number " + userPhoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void updateSubscription_success() {
        long phoneNumber = 80998885566L;
        Subscription newSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        User simpleUser = User.builder()
                .subscription(subscription).build();
        UpdateSubscriptionDTO updateSubscriptionDTO = UpdateSubscriptionDTO.builder()
                .newSubscription(newSubscription).build();
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(simpleUser);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(anyString())).thenReturn(newSubscription);

        userService.updateSubscription(phoneNumber, updateSubscriptionDTO);

        verify(userRepository).save(simpleUser);
    }

    @Test
    public void updateSubscription_phoneNumberNotFound() {
        long phoneNumber = 80998885566L;
        Subscription newSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        UpdateSubscriptionDTO updateSubscriptionDTO = UpdateSubscriptionDTO.builder()
                .newSubscription(newSubscription).build();
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.updateSubscription(phoneNumber, updateSubscriptionDTO);
        });

        assertEquals("User with phone number " + phoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void updateSubscription_subscriptionNotFound() {
        long phoneNumber = 80998885566L;
        Subscription newSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        UpdateSubscriptionDTO updateSubscriptionDTO = UpdateSubscriptionDTO.builder()
                .newSubscription(newSubscription).build();
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(anyString())).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.updateSubscription(phoneNumber, updateSubscriptionDTO);
        });

        assertEquals("Subscription with name "
                + newSubscription.getSubscriptionName() + " not found", exception.getMessage());
    }

    @Test
    public void findAllUsers_success() {
        User firstUser = User.builder().phoneNumber(80996653200L).build();
        User secondUser = User.builder().phoneNumber(80996653277L).build();
        List<User> users = new ArrayList<>();
        users.add(firstUser);
        users.add(secondUser);
        when(userRepository.findAll()).thenReturn(users);

        userService.findAllUsers();

        assertFalse(users.isEmpty());
    }

    @Test
    public void findAllUsers_usersNotFound() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.findAllUsers();
        });

        assertEquals("users not found", exception.getMessage());
    }

    @Test
    public void findUserByPhoneNumber_success() {
        when(userRepository.findByPhoneNumber(80996653200L)).thenReturn(user);

        User findUser = userService.findUserByPhoneNumber(80996653200L);

        assertNotNull(findUser);
    }

    @Test
    public void findUserByPhoneNumber_notFound() {
        long userPhoneNumber = 809789630254L;
        when(userRepository.findByPhoneNumber(809789630254L)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.findUserByPhoneNumber(userPhoneNumber);
        });

        assertEquals("User with phone Number " + userPhoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void findUserByBankCard_success() {
        long bankCardNumber = 2225698763250124L;
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(bankCardNumber).build();
        User user = User.builder()
                .userBankCard(userBankCard).build();
        List<User> users = Collections.singletonList(user);
        userBankCard.setUsers(users);
        when(userBankCardRepository.findByCardNumber(anyLong())).thenReturn(userBankCard);

        List<User> result = userService.findUserByBankCard(bankCardNumber);

        assertNotNull(result);
    }

    @Test
    public void findUserByBankCard_bankCardNotFound() {
        long bankCardNumber = 2225698763250124L;
        when(userBankCardRepository.findByCardNumber(anyLong())).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.findUserByBankCard(bankCardNumber);
        });

        assertEquals("Bank card with number " + bankCardNumber + " not found", exception.getMessage());
    }

    @Test
    public void findUserByBankCard_usersNotFound() {
        long bankCardNumber = 2225698763250124L;
        when(userBankCardRepository.findByCardNumber(anyLong())).thenReturn(userBankCard);
        when(userBankCard.getUsers()).thenReturn(null);
        List<User> users = mock(List.class);
        when(userBankCard.getUsers()).thenReturn(users);
        when(users.isEmpty()).thenReturn(true);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.findUserByBankCard(bankCardNumber);
        });

        assertEquals("No users with bank card " + bankCardNumber, exception.getMessage());
    }

    @Test
    public void findUserBySubscription_success() {
        String userSubscription = "FREE";
        Subscription subscription = Subscription.builder()
                .subscriptionName(userSubscription).build();
        User user = User.builder()
                .subscription(subscription).build();
        List<User> users = Collections.singletonList(user);
        subscription.setUsers(users);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(anyString())).thenReturn(subscription);

        List<User> result = userService.findUserBySubscription(userSubscription);

        assertNotNull(result);
    }

    @Test
    public void findUserBySubscription_subscriptionNotFound() {
        String userSubscription = "FREE";
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(anyString())).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.findUserBySubscription(userSubscription);
        });

        assertEquals("Subscription " + userSubscription + " not found", exception.getMessage());
    }

    @Test
    public void findUserBySubscription_usersNotFound() {
        String userSubscription = "FREE";
        when(subscription.getSubscriptionName()).thenReturn(userSubscription);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(userSubscription))
                .thenReturn(subscription);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.findUserBySubscription(userSubscription);
        });

        assertEquals("No users with subscription " + userSubscription, exception.getMessage());
    }


    @Test
    public void findUserByEmail_success() {
        User user = User.builder().email("example@email.com").build();
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(user);

        User findUser = userService.findUserByEmail("example@email.com");

        assertNotNull(findUser);
    }

    @Test
    public void findUserByEmail_userNotFound() {
        String userEmail = "example.email";
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.findUserByEmail(userEmail);
        });

        assertEquals("User with email " + userEmail + " not found", exception.getMessage());
    }

    @Test
    public void findUserById_success() {
        User user = User.builder().id(1).build();
        when(userRepository.findById(anyLong())).thenReturn(user);

        User findUser = userService.findUserById(1);

        assertNotNull(findUser);
    }

    @Test
    public void findUserById_userNotFound() {
        long userId = 1l;
        when(userRepository.findById(anyLong())).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.findUserById(userId);
        });

        assertEquals("User with id " + userId + " not found", exception.getMessage());
    }

    @Test
    public void deleteUserById_success() {
        long userId = 1L;
        List<User> users = new ArrayList<>();
        UserBankCard simpleUserBankCard = UserBankCard.builder()
                .cardNumber(1478520369874563L)
                .users(users)
                .build();
        User simpleUser = User.builder()
                .id(userId)
                .userBankCard(simpleUserBankCard)
                .telegramChatId("102589")
                .email("email@example.com")
                .build();
        users.add(user);

        when(userRepository.findById(userId)).thenReturn(simpleUser);
        when(userBankCardService.checkBankCardUsers(simpleUserBankCard.getCardNumber())).thenReturn(2);
        doNothing().when(telegramService).notifyUserAboutDeleteAccount("102589");
        doNothing().when(emailService).notifyUserAboutDeleteAccount("email@example.com");

        userService.deleteUserById(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    public void deleteUserById_notFound() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.deleteUserById(userId);
        });

        assertEquals("User with id " + userId + " not found", exception.getMessage());
    }


    @Test
    public void deleteAllUsers_success() {
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(89652301256987456L).build();
        User adminUser = User.builder()
                .phoneNumber(80996665588L).build();
        long adminUserPhoneNumber = 80996665588L;
        User firstUser = User.builder()
                .userBankCard(userBankCard)
                .build();
        User secondUser = User.builder()
                .userBankCard(userBankCard)
                .build();
        List<User> users = new ArrayList<>();
        users.add(firstUser);
        users.add(secondUser);
        when(userRepository.findByPhoneNumber(anyLong())).thenReturn(adminUser);
        when(userRepository.findAll()).thenReturn(users);

        userService.deleteAllUsers(adminUserPhoneNumber);

        verify(userRepository, times(1)).deleteAll(users);
    }

    @Test
    public void deleteUserByPhoneNumber_success() {
        long phoneNumber = 80996586231L;
        String password = "password";
        List<User> users = new ArrayList<>();
        UserBankCard simpleUserBankCard = UserBankCard.builder()
                .cardNumber(1478520369874563L)
                .users(users)
                .build();
        User simpleUser = User.builder()
                .id(phoneNumber)
                .userBankCard(simpleUserBankCard)
                .telegramChatId("102589")
                .email("email@example.com")
                .build();
        users.add(user);

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(simpleUser);
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(userBankCardService.checkBankCardUsers(simpleUserBankCard.getCardNumber())).thenReturn(2);
        doNothing().when(telegramService).notifyUserAboutDeleteAccount("102589");
        doNothing().when(emailService).notifyUserAboutDeleteAccount("email@example.com");

        userService.deleteUserByPhoneNumber(phoneNumber, password);

        verify(userRepository).deleteById(phoneNumber);
    }

    @Test
    public void deleteUserByPhoneNumber_notFound() {
        long phoneNumber = 1L;
        String password = "password";
        User simpleUser = User.builder()
                .phoneNumber(phoneNumber)
                .build();
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(simpleUser);
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUserByPhoneNumber(phoneNumber, password);
        });

        assertEquals("Password is incorrect!", exception.getMessage());
    }

    @Test
    public void deleteUserByPhoneNumber_incorrectPassword() {
        long phoneNumber = 1L;
        String password = "password";
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.deleteUserByPhoneNumber(phoneNumber, password);
        });

        assertEquals("User with phone number " + phoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void deleteUserByBankCardNumber_success() {
        long bankCardNumber = 1478520369874563L;
        long phoneNumber = 80996586231L;
        long adminPhoneNumber = 806632014568L;
        UserBankCard simpleUserBankCard = UserBankCard.builder()
                .cardNumber(bankCardNumber)
                .users(new ArrayList<>())
                .build();
        User simpleUser = User.builder()
                .id(phoneNumber)
                .userBankCard(simpleUserBankCard)
                .telegramChatId("102589")
                .email("email@example.com")
                .build();
        User admin = User.builder()
                .phoneNumber(adminPhoneNumber)
                .userBankCard(simpleUserBankCard)
                .build();
        simpleUserBankCard.getUsers().add(simpleUser);
        simpleUserBankCard.getUsers().add(admin);
        when(userRepository.findByPhoneNumber(adminPhoneNumber)).thenReturn(admin);
        when(userBankCardRepository.findByCardNumber(bankCardNumber)).thenReturn(simpleUserBankCard);
        doNothing().when(telegramService).notifyUserAboutDeleteAccount("102589");
        doNothing().when(emailService).notifyUserAboutDeleteAccount("email@example.com");

        userService.deleteUserByBankCardNumber(bankCardNumber, adminPhoneNumber);

        verify(userRepository).deleteById(phoneNumber);
    }

    @Test
    public void deleteUserByBankCardNumber_bankCardNotFound() {
        long bankCardNumber = 1478520369874563L;
        long phoneNumber = 80996586231L;
        when(userBankCardRepository.findByCardNumber(bankCardNumber)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.deleteUserByBankCardNumber(bankCardNumber, phoneNumber);
        });

        assertEquals("Bank card with number " + bankCardNumber + " not found", exception.getMessage());
    }

    @Test
    public void deleteUsersBySubscription_success() {
        String userSubscription = "FREE";
        long phoneNumber = 80996586231L;
        long adminPhoneNumber = 806632014568L;
        UserBankCard simpleUserBankCard = UserBankCard.builder()
                .cardNumber(1478523698520145L)
                .users(new ArrayList<>())
                .build();
        Subscription simpleSubscription = Subscription.builder()
                .users(new ArrayList<>())
                .subscriptionName(userSubscription)
                .build();
        User simpleUser = User.builder()
                .id(1L)
                .phoneNumber(phoneNumber)
                .telegramChatId("102589")
                .email("email@example.com")
                .subscription(simpleSubscription)
                .userBankCard(simpleUserBankCard)
                .build();
        User admin = User.builder()
                .id(2L)
                .phoneNumber(adminPhoneNumber)
                .subscription(simpleSubscription)
                .userBankCard(simpleUserBankCard)
                .build();
        simpleSubscription.getUsers().add(simpleUser);
        simpleSubscription.getUsers().add(admin);
        simpleUserBankCard.getUsers().add(simpleUser);
        simpleUserBankCard.getUsers().add(admin);
        when(userRepository.findByPhoneNumber(adminPhoneNumber)).thenReturn(admin);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(userSubscription)).thenReturn(simpleSubscription);
        doNothing().when(telegramService).notifyUserAboutDeleteAccount("102589");
        doNothing().when(emailService).notifyUserAboutDeleteAccount("email@example.com");

        userService.deleteUsersBySubscription(adminPhoneNumber, userSubscription);

        verify(userRepository).deleteById(simpleUser.getId());
    }

    @Test
    public void deleteUsersBySubscription_subscriptionNotFound() {
        String userSubscription = "FREE";
        long phoneNumber = 80996586231L;
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(userSubscription)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.deleteUsersBySubscription(phoneNumber, userSubscription);
        });

        assertEquals("Subscription with name " + userSubscription + " not found", exception.getMessage());
    }

    @Test
    public void deleteUserByEmail_success() {
        String userEmail = "email@example.com";
        List<User> users = new ArrayList<>();
        UserBankCard simpleUserBankCard = UserBankCard.builder()
                .cardNumber(1478520369874563L)
                .users(users)
                .build();
        User simpleUser = User.builder()
                .id(1L)
                .userBankCard(simpleUserBankCard)
                .telegramChatId("102589")
                .email("email@example.com")
                .build();
        users.add(user);

        when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(simpleUser);
        when(userBankCardService.checkBankCardUsers(simpleUserBankCard.getCardNumber())).thenReturn(2);
        doNothing().when(telegramService).notifyUserAboutDeleteAccount("102589");
        doNothing().when(emailService).notifyUserAboutDeleteAccount("email@example.com");

        userService.deleteUserByEmail(userEmail);

        verify(userRepository).deleteById(simpleUser.getId());
    }

    @Test
    public void deleteUserByEmail_emailNotFound() {
        String userEmail = "email@example.com";
        when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.deleteUserByEmail(userEmail);
        });

        assertEquals("User with email " + userEmail + " not found", exception.getMessage());
    }

    @Test
    public void setFreeSubscription_success() {
        long phoneNumber = 80996586231L;
        Subscription simpleSubscription = Subscription.builder()
                .subscriptionName("FREE").build();
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(user);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);

        userService.setFreeSubscription(phoneNumber);
        user.setSubscription(simpleSubscription);

        verify(userRepository).save(user);
    }

    @Test
    public void setFreeSubscription_phoneNumberNotFound() {
        long phoneNumber = 80996586231L;
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.setFreeSubscription(phoneNumber);
        });

        assertEquals("User with phone number " + phoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void setFreeSubscription_subscriptionNotFound() {
        long phoneNumber = 80996586231L;
        String subscriptionName = "FREE";
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(user);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(subscriptionName)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.setFreeSubscription(phoneNumber);
        });

        assertEquals("Subscription with subscriptionName " + subscriptionName + " not found", exception.getMessage());
    }

    @Test
    public void findUsersCurrentSubscription_success() {
        long phoneNumber = 80996586231L;
        User simpleUser = User.builder()
                .subscription(Subscription.builder()
                        .subscriptionName("FREE").build()).build();
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(simpleUser);

        userService.findUsersCurrentSubscription(phoneNumber);
        Subscription currentSubscription = simpleUser.getSubscription();

        assertNotNull(currentSubscription);
    }

    @Test
    public void findUsersCurrentSubscription_phoneNumberNotFound() {
        long phoneNumber = 80996586231L;
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.findUsersCurrentSubscription(phoneNumber);
        });

        assertEquals("User with phone number " + phoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void userSubscriptionExpiredTime_success() {
        long phoneNumber = 80996586231L;
        User simpleUser = User.builder()
                .subscription(Subscription.builder()
                        .subscriptionName("FREE").build())
                .endTime(LocalDateTime.now()).build();
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(simpleUser);

        userService.userSubscriptionExpiredTime(phoneNumber);
        LocalDateTime endTime = simpleUser.getEndTime();

        assertNotNull(endTime);
    }

    @Test
    public void userSubscriptionExpiredTime_phoneNumberNotFound() {
        long phoneNumber = 80996586231L;
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.userSubscriptionExpiredTime(phoneNumber);
        });

        assertEquals("User with phone number " + phoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void checkUsersAutoRenewStatus_success() {
        long phoneNumber = 80996586231L;
        User simpleUser = User.builder()
                .subscription(Subscription.builder()
                        .subscriptionName("FREE").build())
                .endTime(LocalDateTime.now())
                .autoRenew(AutoRenewStatus.YES).build();
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(simpleUser);

        userService.checkUsersAutoRenewStatus(phoneNumber);
        AutoRenewStatus autoRenewStatus = simpleUser.getAutoRenew();

        assertNotNull(autoRenewStatus);
    }

    @Test
    public void checkUsersAutoRenewStatus_phoneNumberNotFound() {
        long phoneNumber = 80996586231L;
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.checkUsersAutoRenewStatus(phoneNumber);
        });

        assertEquals("User with phone number " + phoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void getUserBankCard_success() {
        long phoneNumber = 80996586231L;
        User simpleUser = User.builder()
                .subscription(Subscription.builder()
                        .subscriptionName("FREE").build())
                .endTime(LocalDateTime.now())
                .autoRenew(AutoRenewStatus.YES)
                .userBankCard(UserBankCard.builder()
                        .cardNumber(1478963025874563L).build()).build();
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(simpleUser);

        userService.getUserBankCard(phoneNumber);
        UserBankCard simpleUserBankCard = simpleUser.getUserBankCard();

        assertNotNull(simpleUserBankCard);
    }

    @Test
    public void getUserBankCard_phoneNumberNotFound() {
        long phoneNumber = 80996586231L;
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.getUserBankCard(phoneNumber);
        });

        assertEquals("User with phone number " + phoneNumber + " not found", exception.getMessage());
    }

    @Test
    public void getUserEmail_success() {
        long phoneNumber = 80996586231L;
        User simpleUser = User.builder()
                .subscription(Subscription.builder()
                        .subscriptionName("FREE").build())
                .endTime(LocalDateTime.now())
                .autoRenew(AutoRenewStatus.YES)
                .userBankCard(UserBankCard.builder()
                        .cardNumber(1478963025874563L).build())
                .email("email@.com").build();
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(simpleUser);

        userService.getUserEmail(phoneNumber);
        String email = simpleUser.getEmail();

        assertNotNull(email);
    }

    @Test
    public void getUserEmail_phoneNumberNotFound() {
        long phoneNumber = 80996586231L;
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.getUserEmail(phoneNumber);
        });

        assertEquals("User with phone number " + phoneNumber + " not found", exception.getMessage());
    }
}