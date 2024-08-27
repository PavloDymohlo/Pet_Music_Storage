package ua.dymohlo.PetMusicStorage.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ua.dymohlo.PetMusicStorage.PetMusicStorageApplication;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.EmailService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.TelegramService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = PetMusicStorageApplication.class)
@AutoConfigureMockMvc
public class AdminOfficeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;
    @MockBean
    private User user;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private DatabaseUserDetailsService databaseUserDetailsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SubscriptionRepository subscriptionRepository;

    @MockBean
    private TelegramService telegramService;
    @MockBean
    private EmailService emailService;


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUserById_success() throws Exception {
        long userId = 1L;
        doNothing().when(userService).deleteUserById(userId);
        doNothing().when(telegramService).notifyUserAboutDeleteAccount(anyString());
        doNothing().when(emailService).notifyUserAboutDeleteAccount(anyString());

        mvc.perform(delete("/admin_office/delete_user_by_id")
                        .param("id", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id " + userId + " delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUserById_userNotFound() throws Exception {
        long userId = 1L;
        doThrow(new NoSuchElementException("User with id " + userId + " not found")).when(userService).deleteUserById(userId);

        mvc.perform(delete("/admin_office/delete_user_by_id")
                        .param("id", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with id " + userId + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUsersByBankCardNumber_success() throws Exception {
        long userBankCardNumber = 1234567890123456L;
        long userPhoneNumber = 80970011223L;
        when(userService.getCurrentUserPhoneNumber("Bearer " + anyString())).thenReturn(userPhoneNumber);
        doNothing().when(userService).deleteUserByBankCardNumber(userBankCardNumber, userPhoneNumber);
        doNothing().when(telegramService).notifyUserAboutDeleteAccount(anyString());
        doNothing().when(emailService).notifyUserAboutDeleteAccount(anyString());

        mvc.perform(delete("/admin_office/delete_users_by_bank_card_number")
                        .param("bankCardNumber", String.valueOf(userBankCardNumber))
                        .header("Authorization", "Bearer " + anyString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Users with bankCardNumber " + userBankCardNumber + " delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUsersByBankCardNumber_bankCardNotFound() throws Exception {
        long userBankCardNumber = 1234567890123456L;
        long userPhoneNumber = 80990011223L;
        when(userService.getCurrentUserPhoneNumber("Bearer" + anyString())).thenReturn(userPhoneNumber);
        doThrow(new NoSuchElementException("Bank card with number " + userBankCardNumber + " not found"))
                .when(userService).deleteUserByBankCardNumber(userBankCardNumber, userPhoneNumber);

        mvc.perform(delete("/admin_office/delete_users_by_bank_card_number")
                        .param("bankCardNumber", String.valueOf(userBankCardNumber))
                        .header("Authorization", "Bearer" + anyString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Bank card with number " + userBankCardNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUsersBySubscription_success() throws Exception {
        String subscriptionName = "OPTIMAL";
        long userPhoneNumber = 80990011223L;
        when(userService.getCurrentUserPhoneNumber("Bearer" + anyString())).thenReturn(userPhoneNumber);
        doNothing().when(userService).deleteUsersBySubscription(userPhoneNumber, subscriptionName);
        doNothing().when(telegramService).notifyUserAboutDeleteAccount(anyString());
        doNothing().when(emailService).notifyUserAboutDeleteAccount(anyString());

        mvc.perform(delete("/admin_office/delete_users_by_subscription")
                        .param("subscription", subscriptionName)
                        .header("Authorization", "Bearer" + anyString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Users with subscription " + subscriptionName + " delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUsersBySubscription_userNitFound() throws Exception {
        String subscriptionName = "OPTIMAL";
        long userPhoneNumber = 80990011223L;
        when(userService.getCurrentUserPhoneNumber("Bearer" + anyString())).thenReturn(userPhoneNumber);
        doThrow(new NoSuchElementException("Users with subscription " + subscriptionName + " not found"))
                .when(userService).deleteUsersBySubscription(userPhoneNumber, subscriptionName);

        mvc.perform(delete("/admin_office/delete_users_by_subscription")
                        .param("subscription", subscriptionName)
                        .header("Authorization", "Bearer" + anyString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Users with subscription " + subscriptionName + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUserByEmail_success() throws Exception {
        String userEmail = "example@.com";
        doNothing().when(userService).deleteUserByEmail(userEmail);
        doNothing().when(telegramService).notifyUserAboutDeleteAccount(anyString());
        doNothing().when(emailService).notifyUserAboutDeleteAccount(anyString());

        mvc.perform(delete("/admin_office/delete_user_by_email")
                        .param("email", userEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User with email " + userEmail + " delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUserByEmail_userNotFound() throws Exception {
        String userEmail = "example@.com";
        doThrow(new NoSuchElementException("User with email " + userEmail + " not found"))
                .when(userService).deleteUserByEmail(userEmail);

        mvc.perform(delete("/admin_office/delete_user_by_email")
                .param("email", userEmail)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with email " + userEmail + " delete successful"));
    }
}
