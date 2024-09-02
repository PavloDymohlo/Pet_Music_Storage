package ua.dymohlo.PetMusicStorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ua.dymohlo.PetMusicStorage.Enum.AutoRenewStatus;
import ua.dymohlo.PetMusicStorage.PetMusicStorageApplication;
import ua.dymohlo.PetMusicStorage.dto.*;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.EmailService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.SubscriptionService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PetMusicStorageApplication.class)
@AutoConfigureMockMvc
public class PersonalOfficeControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserService userService;
    @MockBean
    private DatabaseUserDetailsService databaseUserDetailsService;
    @MockBean
    private UserDetails userDetails;
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private JWTService jwtService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private SubscriptionRepository subscriptionRepository;
    @MockBean
    private SubscriptionService subscriptionService;

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updatePhoneNumber_success() throws Exception {
        UpdatePhoneNumberDTO request = UpdatePhoneNumberDTO.builder()
                .newPhoneNumber(80663256655L).build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doNothing().when(userService).updatePhoneNumber(currentPhoneNumber, request.getNewPhoneNumber());
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Phone number updated successfully!"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updatePhoneNumber_phoneNumberNotFound() throws Exception {
        UpdatePhoneNumberDTO request = UpdatePhoneNumberDTO.builder()
                .newPhoneNumber(80663256655L).build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doThrow(new NoSuchElementException("Phone number " + currentPhoneNumber + " not found"))
                .when(userService).updatePhoneNumber(currentPhoneNumber, request.getNewPhoneNumber());
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Phone number " + currentPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updatePhoneNumber_phoneNumberAlreadyExists() throws Exception {
        UpdatePhoneNumberDTO request = UpdatePhoneNumberDTO.builder()
                .newPhoneNumber(80663256655L).build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doThrow(new IllegalArgumentException("Phone number " + request.getNewPhoneNumber() + " already exists"))
                .when(userService).updatePhoneNumber(currentPhoneNumber, request.getNewPhoneNumber());
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Phone number " + request.getNewPhoneNumber() + " already exists"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updateSubscription_success() throws Exception {
        UpdateSubscriptionDTO request = UpdateSubscriptionDTO.builder()
                .newSubscription(Subscription.builder()
                        .subscriptionName("FREE").build()).build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(request.getNewSubscription().getSubscriptionName()))
                .thenReturn(subscription);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription " + subscription.getSubscriptionName() + " successfully activated"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updateSubscription_subscriptionNotFound() throws Exception {
        UpdateSubscriptionDTO request = UpdateSubscriptionDTO.builder()
                .newSubscription(Subscription.builder()
                        .subscriptionName("FREE").build()).build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(request.getNewSubscription().getSubscriptionName()))
                .thenReturn(null);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription " + subscription.getSubscriptionName() + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updateBankCard_success() throws Exception {
        UpdateUserBankCardDTO request = UpdateUserBankCardDTO.builder()
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(1234567890123456L).build()).build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doNothing().when(userService).updateBankCard(currentPhoneNumber, request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_bank_card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Bank card for user with phone number "
                        + currentPhoneNumber + " updated successful"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updateBankCard_phoneNumberNotFound() throws Exception {
        UpdateUserBankCardDTO request = UpdateUserBankCardDTO.builder()
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(1234567890123456L).build()).build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doThrow(new NoSuchElementException("Phone number " + currentPhoneNumber + " not found"))
                .when(userService).updateBankCard(eq(currentPhoneNumber), any(UpdateUserBankCardDTO.class));

        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_bank_card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Phone number " + currentPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updateBankCard_invalidCardDetails() throws Exception {
        UpdateUserBankCardDTO request = UpdateUserBankCardDTO.builder()
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(1234567890123456L).build()).build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doThrow(new IllegalArgumentException("Invalid card details for user with phone number " + currentPhoneNumber))
                .when(userService).updateBankCard(eq(currentPhoneNumber), any(UpdateUserBankCardDTO.class));

        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_bank_card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid card details for user with phone number "
                        + currentPhoneNumber));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updatePassword_success() throws Exception {
        UpdatePasswordDTO request = UpdatePasswordDTO.builder()
                .newPassword("newPassword").build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doNothing().when(userService).updatePassword(currentPhoneNumber, request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Password for user with phone number "
                        + currentPhoneNumber + " updated successful"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updatePassword_phoneNumberNotFound() throws Exception {
        UpdatePasswordDTO request = UpdatePasswordDTO.builder()
                .newPassword("newPassword").build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doThrow(new NoSuchElementException("Phone number " + currentPhoneNumber + " not found"))
                .when(userService).updatePassword(currentPhoneNumber, request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Phone number " + currentPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updatePassword_incorrectCurrentPassword() throws Exception {
        UpdatePasswordDTO request = UpdatePasswordDTO.builder()
                .newPassword("newPassword").build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doThrow(new IllegalArgumentException("Current password is incorrect!"))
                .when(userService).updatePassword(currentPhoneNumber, request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Current password is incorrect!"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updateEmail_success() throws Exception {
        UpdateEmailDTO request = UpdateEmailDTO.builder()
                .newEmail("example@.com").build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doNothing().when(userService).updateEmail(currentPhoneNumber, request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Email for user with phone number "
                        + currentPhoneNumber + " updated successful"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updateEmail_phoneNumberNotFound() throws Exception {
        UpdateEmailDTO request = UpdateEmailDTO.builder()
                .newEmail("example@.com").build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doThrow(new NoSuchElementException("Phone number " + currentPhoneNumber + " not found"))
                .when(userService).updateEmail(currentPhoneNumber, request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Phone number " + currentPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void updateEmail_pemailAlreadyExists() throws Exception {
        UpdateEmailDTO request = UpdateEmailDTO.builder()
                .newEmail("example@.com").build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doThrow(new IllegalArgumentException("Email " + request.getNewEmail() + " is already exists"))
                .when(userService).updateEmail(currentPhoneNumber, request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/update_email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email " + request.getNewEmail() + " is already exists"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void setAutoRenewStatus_success() throws Exception {
        SetAutoRenewDTO request = SetAutoRenewDTO.builder()
                .autoRenewStatus(AutoRenewStatus.valueOf("YES")).build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doNothing().when(userService).setAutoRenewStatus(currentPhoneNumber, request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/set_auto_renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Auto renew status for user with phone number "
                        + currentPhoneNumber + " set successfully"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void setAutoRenewStatus_phoneNumberNotFound() throws Exception {
        SetAutoRenewDTO request = SetAutoRenewDTO.builder()
                .autoRenewStatus(AutoRenewStatus.valueOf("YES")).build();
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doThrow(new NoSuchElementException("Phone number " + currentPhoneNumber + " not found"))
                .when(userService).setAutoRenewStatus(currentPhoneNumber, request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/personal_office/set_auto_renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Phone number " + currentPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void findUsersCurrentSubscription_success() throws Exception {
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        when(userService.findUsersCurrentSubscription(currentPhoneNumber)).thenReturn(subscription);
        String subscriptionJson = objectMapper.writeValueAsString(subscription);

        mvc.perform(get("/personal_office/subscription")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().json(subscriptionJson));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void findUsersCurrentSubscription_phoneNumberNotFound() throws Exception {
        long currentPhoneNumber = 80663256699l;
        String jwtToken = "mock_jwt_token";
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();

        when(userService.getCurrentUserPhoneNumber(anyString())).thenReturn(currentPhoneNumber);
        doThrow(new NoSuchElementException("User with phone number " + currentPhoneNumber + " not found"))
                .when(userService).findUsersCurrentSubscription(currentPhoneNumber);
        String subscriptionJson = objectMapper.writeValueAsString(subscription);

        mvc.perform(get("/personal_office/subscription")
                        .header("Authorization", jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with phone number " + currentPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void findAllSubscriptions_success() throws Exception {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);

        when(subscriptionService.findAllSubscription()).thenReturn(subscriptions);
        String subscriptionsJson = objectMapper.writeValueAsString(subscriptions);

        mvc.perform(get("/personal_office/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(content().json(subscriptionsJson));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void findAllSubscriptions_subscriptionsNotFound() throws Exception {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);

        doThrow(new NoSuchElementException("Subscriptions not found"))
                .when(subscriptionService).findAllSubscription();

        mvc.perform(get("/personal_office/subscriptions"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscriptions not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void findSubscriptionsByPrice_success() throws Exception {
        BigDecimal minPrice = BigDecimal.valueOf(0);
        BigDecimal maxPrice = BigDecimal.valueOf(100);
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);

        when(subscriptionService.findSubscriptionsByPrice(minPrice, maxPrice))
                .thenReturn(subscriptions);
        String subscriptionsJson = objectMapper.writeValueAsString(subscriptions);

        mvc.perform(get("/personal_office/subscription_by_price")
                        .param("minPrice", String.valueOf(minPrice))
                        .param("maxPrice", String.valueOf(maxPrice)))
                .andExpect(status().isOk())
                .andExpect(content().json(subscriptionsJson));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void findSubscriptionsByPrice_subscriptionsNotFound() throws Exception {
        BigDecimal minPrice = BigDecimal.valueOf(0);
        BigDecimal maxPrice = BigDecimal.valueOf(100);

        doThrow(new NoSuchElementException("Subscriptions between price " + minPrice + " and " + maxPrice + " not found"))
                .when(subscriptionService).findSubscriptionsByPrice(minPrice, maxPrice);

        mvc.perform(get("/personal_office/subscription_by_price")
                        .param("minPrice", String.valueOf(minPrice))
                        .param("maxPrice", String.valueOf(maxPrice)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscriptions between price " + minPrice + " and "
                        + maxPrice + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void findSubscriptionBySubscriptionName_success() throws Exception {
        String subscriptionName = "FREE";
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();

        when(subscriptionService.findSubscriptionBySubscriptionName(subscriptionName)).thenReturn(subscription);
        String subscriptionJson = objectMapper.writeValueAsString(subscription);

        mvc.perform(get("/personal_office/subscription_by_name")
                        .param("subscriptionName", subscriptionName))
                .andExpect(status().isOk())
                .andExpect(content().json(subscriptionJson));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void findSubscriptionBySubscriptionName_subscriptionNotFound() throws Exception {
        String subscriptionName = "FREE";

        doThrow(new NoSuchElementException("Subscription with subscriptionName " + subscriptionName + " not found"))
                .when(subscriptionService).findSubscriptionBySubscriptionName(subscriptionName);

        mvc.perform(get("/personal_office/subscription_by_name")
                        .param("subscriptionName", subscriptionName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription with subscriptionName "
                        + subscriptionName + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void userSubscriptionExpiredTime_success() throws Exception {
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";
        String subscriptionEndTme = "example_date_time";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        when(userService.userSubscriptionExpiredTime(userPhoneNumber)).thenReturn(subscriptionEndTme);

        mvc.perform(get("/personal_office/subscription_end_time")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string(subscriptionEndTme));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void userSubscriptionExpiredTime_phoneNumberNotFound() throws Exception {
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        doThrow(new NoSuchElementException("User with phone number " + userPhoneNumber + " not found"))
                .when(userService).userSubscriptionExpiredTime(userPhoneNumber);

        mvc.perform(get("/personal_office/subscription_end_time")
                        .header("Authorization", jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with phone number " + userPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void checkUsersAutoRenewStatus_success() throws Exception {
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";
        String autoRenewStatus = "NO";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        when(userService.checkUsersAutoRenewStatus(userPhoneNumber)).thenReturn(autoRenewStatus);

        mvc.perform(get("/personal_office/auto_renew_status")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string(autoRenewStatus));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void checkUsersAutoRenewStatus_phoneNumberNotFound() throws Exception {
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        doThrow(new NoSuchElementException("User with phone number " + userPhoneNumber + " not found"))
                .when(userService).checkUsersAutoRenewStatus(userPhoneNumber);

        mvc.perform(get("/personal_office/auto_renew_status")
                        .header("Authorization", jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with phone number " + userPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void getUserPhoneNumber_success() throws Exception {
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);

        mvc.perform(get("/personal_office/phone_number")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(userPhoneNumber)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void getUserBankCardNumber_success() throws Exception {
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L).build();

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        when(userService.getUserBankCard(userPhoneNumber)).thenReturn(userBankCard);

        mvc.perform(get("/personal_office/bank_card_number")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(userBankCard.getCardNumber())));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void getUserBankCardNumber_phoneNumberNotFound() throws Exception {
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        doThrow(new NoSuchElementException("User with phone number " + userPhoneNumber + " not found"))
                .when(userService).getUserBankCard(userPhoneNumber);

        mvc.perform(get("/personal_office/bank_card_number")
                        .header("Authorization", jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with phone number " + userPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void getUserEmail_success() throws Exception {
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";
        String userEmail = "example@.com";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        when(userService.getUserEmail(userPhoneNumber)).thenReturn(userEmail);

        mvc.perform(get("/personal_office/email")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string(userEmail));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void getUserEmail_phoneNumberNotFound() throws Exception {
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        doThrow(new NoSuchElementException("User with phone number " + userPhoneNumber + " not found"))
                .when(userService).getUserEmail(userPhoneNumber);

        mvc.perform(get("/personal_office/email")
                        .header("Authorization", jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with phone number " + userPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void deleteUserByPhoneNumber_success() throws Exception {
        DeleteUserAccountDTO request = DeleteUserAccountDTO.builder()
                .password("password").build();
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        doNothing().when(userService).deleteUserByPhoneNumber(userPhoneNumber, request.getPassword());
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(delete("/personal_office/delete_user_by_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", jwtToken))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "/host_page"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void deleteUserByPhoneNumber_phoneNumberNotFound() throws Exception {
        DeleteUserAccountDTO request = DeleteUserAccountDTO.builder()
                .password("password").build();
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        doThrow(new NoSuchElementException("User with phone number " + userPhoneNumber + " not found"))
                .when(userService).deleteUserByPhoneNumber(userPhoneNumber, request.getPassword());
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(delete("/personal_office/delete_user_by_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with phone number " + userPhoneNumber + " not found"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"FREE"})
    public void deleteUserByPhoneNumber_incorrectPassword() throws Exception {
        DeleteUserAccountDTO request = DeleteUserAccountDTO.builder()
                .password("password").build();
        long userPhoneNumber = 80665896587L;
        String jwtToken = "mock_jwt_token";

        when(userService.getCurrentUserPhoneNumber(jwtToken)).thenReturn(userPhoneNumber);
        doThrow(new IllegalArgumentException("Password is incorrect!"))
                .when(userService).deleteUserByPhoneNumber(userPhoneNumber, request.getPassword());
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(delete("/personal_office/delete_user_by_phone_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password is incorrect!"));
    }
}