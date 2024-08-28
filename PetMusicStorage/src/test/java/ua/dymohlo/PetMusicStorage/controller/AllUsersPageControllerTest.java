package ua.dymohlo.PetMusicStorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ua.dymohlo.PetMusicStorage.Enum.AutoRenewStatus;
import ua.dymohlo.PetMusicStorage.PetMusicStorageApplication;
import ua.dymohlo.PetMusicStorage.dto.*;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.service.EmailService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PetMusicStorageApplication.class)
@AutoConfigureMockMvc
public class AllUsersPageControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    @MockBean
    private JWTService jwtService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private PaymentController paymentController;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private SubscriptionRepository subscriptionRepository;


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findAllUsers_success() throws Exception {
        User user = User.builder()
                .phoneNumber(80663322110L).build();
        List<User> users = new ArrayList<>();
        users.add(user);
        when(userService.findAllUsers()).thenReturn(users);

        String usersJson = objectMapper.writeValueAsString(users);

        mvc.perform(get("/users/all_users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(usersJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findAllUsers_usersNotFound() throws Exception {
        doThrow(new NoSuchElementException("users not found"))
                .when(userService).findAllUsers();

        mvc.perform(get("/users/all_users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("users not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findUserByPhoneNumber_success() throws Exception {
        User user = User.builder()
                .phoneNumber(80663322110L).build();
        long phoneNumber = 80663322110L;

        when(userService.findUserByPhoneNumber(phoneNumber)).thenReturn(user);
        String userJson = objectMapper.writeValueAsString(user);

        mvc.perform(get("/users/user_by_phone")
                        .param("phoneNumber", String.valueOf(phoneNumber)))
                .andExpect(status().isOk())
                .andExpect(content().json(userJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findUserByPhoneNumber_userNotFound() throws Exception {
        long phoneNumber = 80663322100L;

        doThrow(new NoSuchElementException("User with phone Number " + phoneNumber + " not found"))
                .when(userService).findUserByPhoneNumber(phoneNumber);

        mvc.perform(get("/users/user_by_phone")
                        .param("phoneNumber", String.valueOf(phoneNumber)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with phone Number " + phoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findUserByEmail_success() throws Exception {
        String userEmail = "example@.mail";
        User user = User.builder()
                .email("example@.mail").build();

        when(userService.findUserByEmail(userEmail)).thenReturn(user);
        String userJson = objectMapper.writeValueAsString(user);

        mvc.perform(get("/users/user_by_email")
                        .param("email", userEmail))
                .andExpect(status().isOk())
                .andExpect(content().json(userJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findUserByEmail_userNotFound() throws Exception {
        String userEmail = "example@.mail";

        doThrow(new NoSuchElementException("User with email " + userEmail + " not found"))
                .when(userService).findUserByEmail(userEmail);

        mvc.perform(get("/users/user_by_email")
                        .param("email", userEmail))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with email " + userEmail + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findUserByBankCard_success() throws Exception {
        long bankCardNumber = 1234567890123456L;
        User user = User.builder()
                .userBankCard(UserBankCard.builder()
                        .cardNumber(1234567890123456L).build()).build();
        List<User> users = new ArrayList<>();
        users.add(user);

        when(userService.findUserByBankCard(bankCardNumber)).thenReturn(users);
        String usersJson = objectMapper.writeValueAsString(users);

        mvc.perform(get("/users/user_by_bank_card")
                        .param("bankCardNumber", String.valueOf(bankCardNumber)))
                .andExpect(status().isOk())
                .andExpect(content().json(usersJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findUserByBankCard_bankCardNotFound() throws Exception {
        long bankCardNumber = 1234567890123456L;

        doThrow(new NoSuchElementException("Bank card with number " + bankCardNumber + " not found"))
                .when(userService).findUserByBankCard(bankCardNumber);

        mvc.perform(get("/users/user_by_bank_card")
                        .param("bankCardNumber", String.valueOf(bankCardNumber)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Bank card with number " + bankCardNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findUserById_success() throws Exception {
        long userId = 10l;
        User user = User.builder()
                .id(1L).build();

        when(userService.findUserById(userId)).thenReturn(user);
        String userJson = objectMapper.writeValueAsString(user);

        mvc.perform(get("/users/user_by_id")
                        .param("id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(userJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findUserById_userNotFound() throws Exception {
        long userId = 10l;

        doThrow(new NoSuchElementException("User with id " + userId + " not found"))
                .when(userService).findUserById(userId);

        mvc.perform(get("/users/user_by_id")
                        .param("id", String.valueOf(userId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with id " + userId + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findUserBySubscription_success() throws Exception {
        String subscriptionName = "FREE";
        User user = User.builder()
                .subscription(Subscription.builder()
                        .subscriptionName("FREE").build()).build();
        List<User> users = new ArrayList<>();
        users.add(user);

        when(userService.findUserBySubscription(subscriptionName)).thenReturn(users);
        String usersJson = objectMapper.writeValueAsString(users);

        mvc.perform(get("/users/user_by_subscription")
                        .param("subscription", subscriptionName))
                .andExpect(status().isOk())
                .andExpect(content().json(usersJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findUserBySubscription_subscriptionNotFound() throws Exception {
        String subscriptionName = "PREMIUM";

        doThrow(new NoSuchElementException("Subscription " + subscriptionName + " not found"))
                .when(userService).findUserBySubscription(subscriptionName);

        mvc.perform(get("/users/user_by_subscription")
                        .param("subscription", subscriptionName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription " + subscriptionName + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserPhoneNumber_success() throws Exception {
        UpdatePhoneNumberDTO request = UpdatePhoneNumberDTO.builder()
                .currentPhoneNumber(80999999999L)
                .newPhoneNumber(806632154556L).build();

        doNothing().when(userService).updatePhoneNumber(request.getCurrentPhoneNumber(), request.getNewPhoneNumber());
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("New phone number " + request.getNewPhoneNumber() + " updated successfully!"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserPhoneNumber_phoneNumberNotFound() throws Exception {
        UpdatePhoneNumberDTO request = UpdatePhoneNumberDTO.builder()
                .currentPhoneNumber(80999999999L)
                .newPhoneNumber(806632154556L).build();

        doThrow(new NoSuchElementException("Phone number " + request.getCurrentPhoneNumber() + " not found"))
                .when(userService).updatePhoneNumber(request.getCurrentPhoneNumber(), request.getNewPhoneNumber());
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Phone number " + request.getCurrentPhoneNumber() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserPhoneNumber_phoneNumberAlreadyExists() throws Exception {
        UpdatePhoneNumberDTO request = UpdatePhoneNumberDTO.builder()
                .currentPhoneNumber(80999999999L)
                .newPhoneNumber(806632154556L).build();

        doThrow(new IllegalArgumentException("Phone number " + request.getNewPhoneNumber() + " already exists"))
                .when(userService).updatePhoneNumber(request.getCurrentPhoneNumber(), request.getNewPhoneNumber());
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Phone number " + request.getNewPhoneNumber() + " already exists"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserPassword_success() throws Exception {
        UpdatePasswordDTO request = UpdatePasswordDTO.builder()
                .userPhoneNumber(80999999999L)
                .newPassword("password").build();

        doNothing().when(userService).updatePassword(request.getUserPhoneNumber(), request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Password for user with phone number " + request.getUserPhoneNumber() + " updated successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserPassword_phoneNumberNotFound() throws Exception {
        UpdatePasswordDTO request = UpdatePasswordDTO.builder()
                .userPhoneNumber(80999999999L)
                .newPassword("password").build();

        doThrow(new NoSuchElementException("Phone number " + request.getUserPhoneNumber() + " not found"))
                .when(userService).updatePassword(request.getUserPhoneNumber(), request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Phone number " + request.getUserPhoneNumber() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserPassword_incorrectCurrentPassword() throws Exception {
        UpdatePasswordDTO request = UpdatePasswordDTO.builder()
                .userPhoneNumber(80999999999L)
                .newPassword("password").build();

        doThrow(new IllegalArgumentException("Current password is incorrect!"))
                .when(userService).updatePassword(request.getUserPhoneNumber(), request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Current password is incorrect!"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void setUserAutoRenewStatus_success() throws Exception {
        SetAutoRenewDTO request = SetAutoRenewDTO.builder()
                .userPhoneNumber(80999999999L)
                .autoRenewStatus(AutoRenewStatus.valueOf("NO")).build();

        doNothing().when(userService).setAutoRenewStatus(request.getUserPhoneNumber(), request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/set_auto_renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Auto renew status for user with phone number " + request.getUserPhoneNumber() + " set successfully"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void setUserAutoRenewStatus_phoneNumberNotFound() throws Exception {
        SetAutoRenewDTO request = SetAutoRenewDTO.builder()
                .userPhoneNumber(80999999999L)
                .autoRenewStatus(AutoRenewStatus.valueOf("NO")).build();

        doThrow(new NoSuchElementException("Phone number " + request.getUserPhoneNumber() + " not found"))
                .when(userService).setAutoRenewStatus(request.getUserPhoneNumber(), request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/set_auto_renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Phone number " + request.getUserPhoneNumber() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserEmail_success() throws Exception {
        UpdateEmailDTO request = UpdateEmailDTO.builder()
                .userPhoneNumber(80999999999L)
                .newEmail("example@.com").build();

        doNothing().when(userService).updateEmail(request.getUserPhoneNumber(), request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Email for user with phone number " + request.getUserPhoneNumber() + " updated successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserEmail_phoneNumberNotFound() throws Exception {
        UpdateEmailDTO request = UpdateEmailDTO.builder()
                .userPhoneNumber(80999999999L)
                .newEmail("example@.com").build();

        doThrow(new NoSuchElementException("Phone number " + request.getUserPhoneNumber() + " not found"))
                .when(userService).updateEmail(request.getUserPhoneNumber(), request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Phone number " + request.getUserPhoneNumber() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserEmail_emailAlreadyExists() throws Exception {
        UpdateEmailDTO request = UpdateEmailDTO.builder()
                .userPhoneNumber(80999999999L)
                .newEmail("example@.com").build();

        doThrow(new IllegalArgumentException("Email " + request.getNewEmail() + " is already exists"))
                .when(userService).updateEmail(request.getUserPhoneNumber(), request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email " + request.getNewEmail() + " is already exists"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserBankCard_success() throws Exception {
        UpdateUserBankCardDTO request = UpdateUserBankCardDTO.builder()
                .userPhoneNumber(80999999999L)
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(123456789012346L).build()).build();

        doNothing().when(userService).updateBankCard(request.getUserPhoneNumber(), request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_bank_card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Bank card for user with phone number " + request.getUserPhoneNumber() + " updated successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserBankCard_phoneNumberNotFound() throws Exception {
        UpdateUserBankCardDTO request = UpdateUserBankCardDTO.builder()
                .userPhoneNumber(80999999999L)
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(123456789012346L).build()).build();

        doThrow(new NoSuchElementException("Phone number " + request.getUserPhoneNumber() + " not found"))
                .when(userService).updateBankCard(eq(request.getUserPhoneNumber()), any(UpdateUserBankCardDTO.class));
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_bank_card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Phone number " + request.getUserPhoneNumber() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserBankCard_invalidCardDetails() throws Exception {
        UpdateUserBankCardDTO request = UpdateUserBankCardDTO.builder()
                .userPhoneNumber(80999999999L)
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(123456789012346L).build()).build();

        doThrow(new IllegalArgumentException("Invalid card details for user with phone number " + request.getUserPhoneNumber()))
                .when(userService).updateBankCard(eq(request.getUserPhoneNumber()), any(UpdateUserBankCardDTO.class));
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_bank_card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid card details for user with phone number " + request.getUserPhoneNumber()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserSubscription_success() throws Exception {
        User user = User.builder()
                .phoneNumber(80999999999L)
                .userBankCard(UserBankCard.builder()
                        .cardNumber(1402569855236987L).build()).build();
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        UpdateSubscriptionDTO request = UpdateSubscriptionDTO.builder()
                .userPhoneNumber(80999999999L)
                .newSubscription(Subscription.builder()
                        .subscriptionName("FREE").build()).build();

        when(userRepository.findByPhoneNumber(80999999999L)).thenReturn(user);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);
        when(paymentController.payment(any(TransactionDTO.class)))
                .thenReturn(ResponseEntity.ok("Payment successful"));
        doNothing().when(userService).updateSubscription(request.getUserPhoneNumber(), request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription for user with phone number " + request.getUserPhoneNumber() + " updated successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserSubscription_phoneNumberNotFound() throws Exception {
        User user = User.builder()
                .phoneNumber(80999999999L)
                .userBankCard(UserBankCard.builder()
                        .cardNumber(1402569855236987L).build()).build();
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        UpdateSubscriptionDTO request = UpdateSubscriptionDTO.builder()
                .userPhoneNumber(80999999999L)
                .newSubscription(Subscription.builder()
                        .subscriptionName("FREE").build()).build();

        when(userRepository.findByPhoneNumber(80999999999L)).thenReturn(user);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);
        when(paymentController.payment(any(TransactionDTO.class)))
                .thenReturn(ResponseEntity.ok("Payment successful"));
        doThrow(new NoSuchElementException("User with phone number " + request.getUserPhoneNumber() + " not found"))
                .when(userService).updateSubscription(eq(request.getUserPhoneNumber()), any(UpdateSubscriptionDTO.class));
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with phone number " + request.getUserPhoneNumber() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserSubscription_paymentFailed() throws Exception {
        User user = User.builder()
                .phoneNumber(80999999999L)
                .userBankCard(UserBankCard.builder()
                        .cardNumber(1402569855236987L)
                        .cardExpirationDate("12/25")
                        .cvv((short) 123).build()).build();
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE")
                .subscriptionPrice(BigDecimal.valueOf(0.0)).build();
        UpdateSubscriptionDTO request = UpdateSubscriptionDTO.builder()
                .userPhoneNumber(80999999999L)
                .newSubscription(Subscription.builder()
                        .subscriptionName("FREE")
                        .subscriptionPrice(BigDecimal.valueOf(0.0)).build()).build();

        when(userRepository.findByPhoneNumber(80999999999L)).thenReturn(user);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase("FREE")).thenReturn(subscription);
        when(paymentController.payment(any(TransactionDTO.class)))
                .thenReturn(ResponseEntity.badRequest().body("Payment failed"));
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/users/update_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment failed"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteAllUsers_success() throws Exception {
        long adminPhoneNumber = 80999998866L;

        when(userService.getCurrentUserPhoneNumber("Bearer" + anyString())).thenReturn(adminPhoneNumber);
        doNothing().when(userService).deleteAllUsers(adminPhoneNumber);

        mvc.perform(delete("/users/delete_all_users")
                        .header("Authorization", "Bearer " + anyString()))
                .andExpect(status().isOk())
                .andExpect(content().string("All users deleted successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUserByPhoneNumber_success() throws Exception {
        long phoneNumber = 80999998866L;
        String password = "password";

        doNothing().when(userService).deleteUserByPhoneNumber(phoneNumber, password);
        String passwordJson = objectMapper.writeValueAsString(password);

        mvc.perform(delete("/users/delete_user_by_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson)
                        .param("phoneNumber", String.valueOf(phoneNumber)))
                .andExpect(status().isOk())
                .andExpect(content().string("User with phoneNumber " + phoneNumber + " delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUserByPhoneNumber_phoneNumberNotFound() throws Exception {
        long phoneNumber = 80999998866L;
        String password = "password";

        doThrow(new NoSuchElementException("User with phone number " + phoneNumber + " not found"))
                .when(userService).deleteUserByPhoneNumber(phoneNumber, password);

        mvc.perform(delete("/users/delete_user_by_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(password)
                        .param("phoneNumber", String.valueOf(phoneNumber)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with phone number " + phoneNumber + " not found"));
    }
}