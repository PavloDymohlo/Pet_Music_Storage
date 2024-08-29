package ua.dymohlo.PetMusicStorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ua.dymohlo.PetMusicStorage.Enum.AutoRenewStatus;
import ua.dymohlo.PetMusicStorage.configuration.TestSecurityConfig;
import ua.dymohlo.PetMusicStorage.dto.TransactionDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateSubscriptionDTO;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.repository.SubscriptionRepository;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutoRenewSubscriptionController.class)
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
public class AutoRenewSubscriptionTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SubscriptionRepository subscriptionRepository;

    @MockBean
    private PaymentController paymentController;

    @MockBean
    private UserService userService;

    @MockBean
    private JWTService jwtService;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private User user;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void autoRenewSubscription_success() throws Exception {
        UserBankCard userBankCard = UserBankCard.builder()
                .cardNumber(1234567890123456L)
                .cardExpirationDate("12/25")
                .cvv((short) 111).build();
        Subscription subscription = Subscription.builder()
                .subscriptionName("OPTIMAL")
                .subscriptionPrice(BigDecimal.valueOf(150)).build();
        user = User.builder()
                .phoneNumber(911L)
                .subscription(subscription)
                .userBankCard(userBankCard)
                .autoRenew(AutoRenewStatus.valueOf("YES")).build();
        UpdateSubscriptionDTO updateSubscriptionDTO = UpdateSubscriptionDTO.builder()
                .userPhoneNumber(user.getPhoneNumber())
                .newSubscription(subscription).build();

        when(subscriptionRepository.findBySubscriptionNameIgnoreCase(user.getSubscription().getSubscriptionName())).thenReturn(subscription);
        when(paymentController.payment(any(TransactionDTO.class)))
                .thenReturn(ResponseEntity.ok("Payment successful"));
        doNothing().when(userService).updateSubscription(user.getPhoneNumber(), updateSubscriptionDTO);
        String userJson = objectMapper.writeValueAsString(user);

        mvc.perform(post("/auto_renew_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription for user with phone number " + user.getPhoneNumber() + " updated successful"));
    }
}
