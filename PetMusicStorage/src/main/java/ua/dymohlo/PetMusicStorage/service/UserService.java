package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.dymohlo.PetMusicStorage.Enum.AutoRenewStatus;
import ua.dymohlo.PetMusicStorage.dto.UpdatePasswordDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateUserBankCardDTO;
import ua.dymohlo.PetMusicStorage.dto.UserLoginInDTO;
import ua.dymohlo.PetMusicStorage.dto.UserRegistrationDTO;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.repository.UserBankCardRepository;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;

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
            throw new IllegalArgumentException("Phone number already exists");
        }
        if (userEmailExists(userDTO.getEmail())) {
            log.error("Email {} already exists", userDTO.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(userDTO.getUserBankCard().getCardNumber())
                .cvv(userDTO.getUserBankCard().getCvv())
                .cardExpirationDate(userDTO.getUserBankCard().getCardExpirationDate()).build();
        UserBankCard existingCard = userBankCardRepository.findByCardNumber(userBankCard.getCardNumber());//(UserBankCard) перед userBankCardRepository
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
            throw new IllegalArgumentException("This phone number not found!");
        }
        User user = userRepository.findByPhoneNumber(userLoginInDTO.getPhoneNumber());
        if (!passwordEncoder.matches(userLoginInDTO.getPassword(), user.getPassword())) {
            log.error("Incorrect password for user with phone number: {}", userLoginInDTO.getPhoneNumber());
            throw new IllegalArgumentException("Incorrect password!");
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
            throw new IllegalArgumentException("Phone number not found");
        }
        if (userPhoneNumberExists(newPhoneNumber)) {
            log.error("Phone number {} already exists ", newPhoneNumber);
            throw new IllegalArgumentException("Phone number already exists");
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
            throw new IllegalArgumentException("Phone number not found");
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
                log.error("Invalid details for card: {}",newUserBankCard);
                throw new IllegalArgumentException("Invalid card details");
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
            throw new IllegalArgumentException("Phone number not found");
        }
        User user = userRepository.findByPhoneNumber(userPhoneNumber);
        user.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated successful for user with id: {}", user);
    }

    public void setAutoRenewStatus(long phoneNumber, AutoRenewStatus status) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user != null) {
            user.setAutoRenew(AutoRenewStatus.YES);
            userRepository.save(user);
            log.info("Auto renew status set successfully for user with phone number: {}", phoneNumber);
        }
    }
}
