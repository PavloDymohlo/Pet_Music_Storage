package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final DatabaseUserDetailsService databaseUserDetailsService;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserBankCardRepository userBankCardRepository;
    private final JWTService jwtService;
    private final UserBankCardService userBankCardService;


    public UserDetailsService userDetailsService() {
        return phoneNumber -> getUserByPhoneNumber(Long.parseLong(phoneNumber));
    }

    private UserDetails getUserByPhoneNumber(long phoneNumber) {
        long phoneNumberValue = phoneNumber;
        return databaseUserDetailsService.loadUserByUsername(String.valueOf(phoneNumberValue));
    }

    public User registerUser(UserRegistrationDTO userDTO) {
        if (userPhoneNumberExists(userDTO.getPhoneNumber())) {
            log.error("Phone number {} already exists ", userDTO.getPhoneNumber());
            throw new IllegalArgumentException("Phone number " + userDTO.getPhoneNumber() + " already exists");
        }
        if (userEmailExists(userDTO.getEmail())) {
            log.error("Email {} already exists", userDTO.getEmail());
            throw new IllegalArgumentException("Email " + userDTO.getEmail() + " already exists");
        }
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(userDTO.getUserBankCard().getCardNumber())
                .cvv(userDTO.getUserBankCard().getCvv())
                .cardExpirationDate(userDTO.getUserBankCard().getCardExpirationDate()).build();
        UserBankCard existingCard = userBankCardRepository.findByCardNumber(userBankCard.getCardNumber());
        if (existingCard == null) {
            userBankCardRepository.save(userBankCard);
        } else {
            userBankCard = existingCard;
        }
        Subscription firstSubscription = subscriptionRepository.findBySubscriptionName("MAXIMUM");
        subscriptionRepository.existsBySubscriptionName(String.valueOf(firstSubscription));
        User user = User.builder()
                .phoneNumber(userDTO.getPhoneNumber())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .autoRenew(AutoRenewStatus.YES)
                .email(userDTO.getEmail())
                .subscription(firstSubscription)
                .userBankCard(userBankCard)
                .endTime(LocalDateTime.now().plusMinutes(firstSubscription.getSubscriptionDurationTime()))
                .build();
        return userRepository.save(user);
    }

    private boolean userPhoneNumberExists(long phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    private boolean userEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isPhoneNumberRegistered(long phoneNumber) {
        return userPhoneNumberExists(phoneNumber);
    }

    public boolean isEmailRegistered(String email) {
        return userEmailExists(email);
    }

    public String loginIn(UserLoginInDTO userLoginInDTO) {
        if (!userPhoneNumberExists(userLoginInDTO.getPhoneNumber())) {
            log.error("Invalid phone number: {}", userLoginInDTO.getPhoneNumber());
            throw new NoSuchElementException("Phone number " + userLoginInDTO.getPhoneNumber() + " not found");
        }
        User user = userRepository.findByPhoneNumber(userLoginInDTO.getPhoneNumber());
        if (!passwordEncoder.matches(userLoginInDTO.getPassword(), user.getPassword())) {
            log.error("Incorrect password for user with phone number: {}", userLoginInDTO.getPhoneNumber());
            throw new IllegalArgumentException("Incorrect password for user with phone number " + userLoginInDTO.getPhoneNumber());
        }
        log.info("User login successful: {}", userLoginInDTO.getPhoneNumber());
        return "Success";
    }

    public boolean isAdminSubscription(long phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        Subscription subscription = user.getSubscription();
        return subscription == subscriptionRepository.findBySubscriptionName("ADMIN");
    }

    public void updatePhoneNumber(long currentPhoneNumber, long newPhoneNumber) {
        if (!userPhoneNumberExists(currentPhoneNumber)) {
            log.error("User with phone number: {} does not exists", currentPhoneNumber);
            throw new NoSuchElementException("Phone number " + currentPhoneNumber + " not found");
        }
        if (userPhoneNumberExists(newPhoneNumber)) {
            log.error("Phone number {} already exists ", newPhoneNumber);
            throw new IllegalArgumentException("Phone number " + newPhoneNumber + " already exists");
        }
        User user = userRepository.findByPhoneNumber(currentPhoneNumber);
        user.setPhoneNumber(newPhoneNumber);
        userRepository.save(user);
        log.info("Phone number updated successfully for user with ID: {}", user);
    }

    public long getCurrentUserPhoneNumber(String jwtToken) {
        String userPhoneNumber = getUserPhoneNumberFromToken(jwtToken);
        log.info("User phone number extracted from token: {}", userPhoneNumber);
        return Long.parseLong(userPhoneNumber);
    }

    private String getUserPhoneNumberFromToken(String jwtToken) {
        String parseToken = jwtToken.substring("Bearer".length()).trim();
        log.debug("Parsed token: {}", parseToken);
        return jwtService.extractUserName(parseToken);
    }

    public void updateBankCard(long userPhoneNumber, UpdateUserBankCardDTO updateUserBankCardDTO) {
        if (!userPhoneNumberExists(userPhoneNumber)) {
            log.error("User with phone number: {} does not exists", updateUserBankCardDTO.getUserPhoneNumber());
            throw new NoSuchElementException("Phone number " + userPhoneNumber + " not found");
        }
        UserBankCard newUserBankCard = UserBankCard.builder()
                .cardNumber(updateUserBankCardDTO.getNewUserBankCard().getCardNumber())
                .cardExpirationDate(updateUserBankCardDTO.getNewUserBankCard().getCardExpirationDate())
                .cvv(updateUserBankCardDTO.getNewUserBankCard().getCvv()).build();
        UserBankCard existingCard = userBankCardRepository.findByCardNumber(updateUserBankCardDTO.getNewUserBankCard().getCardNumber());
        if (existingCard == null) {
            userBankCardRepository.save(newUserBankCard);
        } else {
            if (userBankCardService.validateBankCard(newUserBankCard)) {
                newUserBankCard = existingCard;
            } else {
                log.error("Invalid details for card: {}", newUserBankCard);
                throw new IllegalArgumentException("Invalid card details for user with phone number " + userPhoneNumber);
            }
        }
        User user = userRepository.findByPhoneNumber(userPhoneNumber);
        user.setUserBankCard(newUserBankCard);
        userRepository.save(user);
        log.info("Bank card updated successful for user with id: {}", user);
    }

    public void updatePassword(long userPhoneNumber, UpdatePasswordDTO updatePasswordDTO) {
        if (!userPhoneNumberExists(userPhoneNumber)) {
            log.error("User with phone number: {} does not exists", updatePasswordDTO.getUserPhoneNumber());
            throw new NoSuchElementException("Phone number " + userPhoneNumber + " not found");
        }
        User user = userRepository.findByPhoneNumber(userPhoneNumber);
        user.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated successful for user with id: {}", user);
    }

    public void updateEmail(long userPhoneNumber, UpdateEmailDTO updateEmailDTO) {
        if (!userPhoneNumberExists(userPhoneNumber)) {
            log.error("User with phone number: {} does not exists", updateEmailDTO.getUserPhoneNumber());
            throw new NoSuchElementException("Phone number " + userPhoneNumber + " not found");
        }
        if (userRepository.existsByEmail(updateEmailDTO.getNewEmail())) {
            log.error("Email: {} is already exists", updateEmailDTO.getNewEmail());
            throw new IllegalArgumentException("Email " + updateEmailDTO.getNewEmail() + " is already exists");
        }
        User user = userRepository.findByPhoneNumber(userPhoneNumber);
        user.setEmail(updateEmailDTO.getNewEmail());
        userRepository.save(user);
        log.info("Email updated successful for user with id: {}", user);
    }

    public void setAutoRenewStatus(long userPhoneNumber, SetAutoRenewDTO status) {
        if (!userPhoneNumberExists(userPhoneNumber)) {
            log.error("User with phone number: {} does not exists", status.getUserPhoneNumber());
            throw new NoSuchElementException("Phone number " + userPhoneNumber + " not found");
        }
        User user = userRepository.findByPhoneNumber(userPhoneNumber);
        user.setAutoRenew(status.getAutoRenewStatus());
        userRepository.save(user);
        log.info("Auto renew status set successfully for user with phone number: {}", status.getUserPhoneNumber());
    }

    public void updateSubscription(long userPhoneNumber, UpdateSubscriptionDTO updateSubscriptionDTO) {
        if (!userRepository.existsByPhoneNumber(userPhoneNumber)) {
            log.error("User with phone number: {} does not exist", userPhoneNumber);
            throw new NoSuchElementException("User with phone number " + userPhoneNumber + " not found");
        }
        Subscription newSubscription = updateSubscriptionDTO.getNewSubscription();
        Subscription existingSubscription = subscriptionRepository.findBySubscriptionName(newSubscription.getSubscriptionName());
        if (existingSubscription == null) {
            log.error("Subscription with name: {} does not exist", newSubscription.getSubscriptionName());
            throw new NoSuchElementException("Subscription with name " + newSubscription.getSubscriptionName() + " not found");
        }
        User user = userRepository.findByPhoneNumber(userPhoneNumber);
        user.setSubscription(existingSubscription);
        user.setEndTime(LocalDateTime.now().plusMinutes(existingSubscription.getSubscriptionDurationTime()));
        userRepository.save(user);
        log.info("Subscription updated successfully for user with phone number: {}", userPhoneNumber);
    }

    public List<User> findAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new NoSuchElementException("users not found");
        }
        return users;
    }

    public User findUserByPhoneNumber(long userPhoneNumber) {
        User user = userRepository.findByPhoneNumber(userPhoneNumber);
        if (user == null) {
            throw new NoSuchElementException("User with phone Number " + userPhoneNumber + " not found");
        }
        return user;
    }

    public List<User> findUserByBankCard(long userBankCardNumber) {
        UserBankCard userBankCard = userBankCardRepository.findByCardNumber(userBankCardNumber);
        if (userBankCard == null) {
            throw new NoSuchElementException("Bank card with number " + userBankCardNumber + " not found");
        }
        List<User> users = userBankCard.getUsers();
        if (users.isEmpty()) {
            throw new NoSuchElementException("No users with bank card " + userBankCardNumber);
        }
        return users;
    }

    public List<User> findUserBySubscription(String userSubscription) {
        Subscription subscription = subscriptionRepository.findBySubscriptionName(userSubscription);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription " + userSubscription + " not found");
        }
        List<User> users = subscription.getUsers();
        if (users.isEmpty()) {
            throw new NoSuchElementException("No users with subscription " + subscription.getSubscriptionName());
        }
        return users;
    }

    public User findUserByEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new NoSuchElementException("User with email " + userEmail + " not found");
        }
        return user;
    }

    public User findUserById(long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NoSuchElementException("User with id " + userId + " not found");
        }
        return user;
    }

    @Transactional
    public void deleteUserById(long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NoSuchElementException("User with id " + userId + " not found");
        }
        deleteUserFromDataBase(user);
    }

    /*
    This method removes all users except the admin who performed this operation.
    */
    @Transactional
    public void deleteAllUsers(long phoneNumber) {
        User adminUser = userRepository.findByPhoneNumber(phoneNumber);
        List<User> users = userRepository.findAll().stream()
                .filter(user -> user != adminUser)
                .toList();
        userRepository.deleteAll(users);

        List<UserBankCard> userBankCards = users.stream()
                .map(User::getUserBankCard)
                .filter(userBankCard -> userBankCard != null && !userBankCard.equals(adminUser.getUserBankCard()))
                .distinct()
                .collect(Collectors.toList());
        userBankCardRepository.deleteAll(userBankCards);
    }

    @Transactional
    public void deleteUserByPhoneNumber(long phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new NoSuchElementException("User with phone number " + phoneNumber + " not found");
        }
        deleteUserFromDataBase(user);
    }

    @Transactional
    public void deleteUserByBankCardNumber(long bankCardNumber, long phoneNumber) {
        User adminUser = userRepository.findByPhoneNumber(phoneNumber);
        UserBankCard findUserBankCard = userBankCardRepository.findByCardNumber(bankCardNumber);
        if (findUserBankCard == null) {
            throw new NoSuchElementException("Bank card with number " + bankCardNumber + " not found");
        }
        List<User> users = findUserBankCard.getUsers().stream()
                .filter(user -> user != adminUser)
                .toList();
        users.forEach(this::deleteUserFromDataBase);
    }

    private void deleteUserFromDataBase(User user) {
        if (userBankCardService.checkBankCardUsers(user.getUserBankCard().getCardNumber()) == 1) {
            userBankCardService.deleteBankCard(user.getUserBankCard().getCardNumber());
        } else {
            UserBankCard userBankCard = user.getUserBankCard();
            if (userBankCard != null) {
                userBankCard.getUsers().remove(user);
                if (userBankCard.getUsers().isEmpty()) {
                    userBankCardService.deleteBankCard(userBankCard.getCardNumber());
                }
            }
            userRepository.deleteById(user.getId());
        }
    }

    @Transactional
    public void deleteUsersBySubscription(long phoneNumber, String userSubscription) {
        User adminUser = userRepository.findByPhoneNumber(phoneNumber);
        Subscription subscription = subscriptionRepository.findBySubscriptionName(userSubscription);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with name " + userSubscription + " not found");
        }
        List<User> users = subscription.getUsers().stream()
                .filter(user -> user != adminUser)
                .toList();
        if (subscription.getUsers().isEmpty()) {
            throw new NoSuchElementException("Users with subscription " + userSubscription + " not found");
        }
        subscription.getUsers().removeAll(users);
        users.forEach(this::deleteUserFromDataBase);
    }

    @Transactional
    public void deleteUserByEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new NoSuchElementException("User with email " + userEmail + " not found");
        }
        deleteUserFromDataBase(user);
    }

    public void setFreeSubscription(long userPhoneNumber) {
        User user = userRepository.findByPhoneNumber(userPhoneNumber);
        if (user == null) {
            throw new NoSuchElementException("User with phone number " + userPhoneNumber + " not found");
        }
        String subscriptionName = "FREE";
        Subscription subscription = subscriptionRepository.findBySubscriptionName(subscriptionName);
        if (subscription == null) {
            throw new NoSuchElementException("Subscription with subscriptionName " + subscriptionName + " not found");
        }
        user.setSubscription(subscription);
        userRepository.save(user);
    }
}