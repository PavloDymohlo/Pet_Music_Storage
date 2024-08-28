package ua.dymohlo.PetMusicStorage.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
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
import ua.dymohlo.PetMusicStorage.dto.*;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.service.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PetMusicStorageApplication.class)
@AutoConfigureMockMvc
public class AdminOfficeControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    @MockBean
    private SubscriptionService subscriptionService;
    @MockBean
    private JWTService jwtService;
    @MockBean
    private TelegramService telegramService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private MusicFileService musicFileService;
    @MockBean
    private BankTransactionDataService bankTransactionDataService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUserById_success() throws Exception {
        long userId = 1L;
        doNothing().when(userService).deleteUserById(userId);
        doNothing().when(telegramService).notifyUserAboutDeleteAccount(anyString());
        doNothing().when(emailService).notifyUserAboutDeleteAccount(anyString());

        mvc.perform(delete("/admin_office/delete_user_by_id")
                        .param("id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id " + userId + " delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteUserById_userNotFound() throws Exception {
        long userId = 1L;
        doThrow(new NoSuchElementException("User with id " + userId + " not found")).when(userService).deleteUserById(userId);

        mvc.perform(delete("/admin_office/delete_user_by_id")
                        .param("id", String.valueOf(userId)))
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
                        .header("Authorization", "Bearer " + anyString()))
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
                        .header("Authorization", "Bearer" + anyString()))
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
                        .header("Authorization", "Bearer" + anyString()))
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
                        .header("Authorization", "Bearer" + anyString()))
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
                        .param("email", userEmail))
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
                        .param("email", userEmail))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with email " + userEmail + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findSubscriptionById_success() throws Exception {
        Subscription subscription = Subscription.builder()
                .id(1L).build();
        long subscriptionId = 1L;
        when(subscriptionService.findSubscriptionById(1L)).thenReturn(subscription);

        mvc.perform(get("/admin_office/subscription_by_id")
                        .param("id", String.valueOf(subscriptionId)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1}"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findSubscriptionById_subscriptionNotFound() throws Exception {
        long subscriptionId = 1L;
        when(subscriptionService.findSubscriptionById(anyLong())).thenReturn(null);
        doThrow(new NoSuchElementException("Subscription with id " + subscriptionId + " not found"))
                .when(subscriptionService).findSubscriptionById(subscriptionId);

        mvc.perform(get("/admin_office/subscription_by_id")
                        .param("id", String.valueOf(subscriptionId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription with id " + subscriptionId + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findAllSubscriptions_success() throws Exception {
        Subscription firstSubscription = Subscription.builder()
                .subscriptionName("FREE").build();
        Subscription secondSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(firstSubscription);
        subscriptions.add(secondSubscription);

        when(subscriptionService.findAllSubscription()).thenReturn(subscriptions);
        String subscriptionListJson = "[{\"subscriptionName\":\"FREE\"},{\"subscriptionName\":\"MAXIMUM\"}]";

        mvc.perform(get("/admin_office/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(content().json(subscriptionListJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findAllSubscriptions_subscriptionsNotFound() throws Exception {
        when(subscriptionService.findAllSubscription()).thenReturn(null);

        doThrow(new NoSuchElementException("Subscriptions not found"))
                .when(subscriptionService).findAllSubscription();

        mvc.perform(get("/admin_office/subscriptions"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscriptions not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findSubscriptionsByPrice_success() throws Exception {
        Subscription firstSubscription = Subscription.builder()
                .subscriptionName("FREE").build();
        Subscription secondSubscription = Subscription.builder()
                .subscriptionName("MAXIMUM").build();
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(firstSubscription);
        subscriptions.add(secondSubscription);

        FindSubscriptionsByPriceDTO request = FindSubscriptionsByPriceDTO.builder()
                .minPrice(BigDecimal.valueOf(0))
                .maxPrice(BigDecimal.valueOf(300)).build();

        when(subscriptionService.findSubscriptionsByPrice(request.getMinPrice(), request.getMaxPrice()))
                .thenReturn(subscriptions);

        mvc.perform(get("/admin_office/subscription_by_price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(subscriptions)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findSubscriptionsByPrice_subscriptionsNotFound() throws Exception {
        FindSubscriptionsByPriceDTO request = FindSubscriptionsByPriceDTO.builder()
                .minPrice(BigDecimal.valueOf(0))
                .maxPrice(BigDecimal.valueOf(300)).build();
        when(subscriptionService.findSubscriptionsByPrice(request.getMinPrice(), request.getMaxPrice()))
                .thenReturn(null);

        doThrow(new NoSuchElementException("Subscriptions between price " + request.getMinPrice() + " and " + request.getMaxPrice() + " not found"))
                .when(subscriptionService).findSubscriptionsByPrice(request.getMinPrice(), request.getMaxPrice());

        mvc.perform(get("/admin_office/subscription_by_price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscriptions between price " + request.getMinPrice() + " and " + request.getMaxPrice() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findSubscriptionBySubscriptionName_success() throws Exception {
        String findSubscriptionName = "FREE";
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();

        when(subscriptionService.findSubscriptionBySubscriptionName(anyString())).thenReturn(subscription);

        mvc.perform(get("/admin_office/subscription_by_name")
                        .param("subscriptionName", findSubscriptionName))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"subscriptionName\":FREE}"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findSubscriptionBySubscriptionName_subscriptionNotFound() throws Exception {
        String findSubscriptionName = "PREMIUM";
        when(subscriptionService.findSubscriptionBySubscriptionName(anyString())).thenReturn(null);

        doThrow(new NoSuchElementException("Subscription with subscriptionName " + findSubscriptionName + " not found"))
                .when(subscriptionService).findSubscriptionBySubscriptionName(findSubscriptionName);

        mvc.perform(get("/admin_office/subscription_by_name")
                        .param("subscriptionName", findSubscriptionName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription with subscriptionName " + findSubscriptionName + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateSubscriptionName_success() throws Exception {
        UpdateSubscriptionNameDTO request = UpdateSubscriptionNameDTO.builder()
                .currentSubscriptionName("FREE")
                .newSubscriptionName("PREMIUM").build();

        doNothing().when(subscriptionService).updateSubscriptionName(request);

        mvc.perform(put("/admin_office/update_subscription_name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentSubscriptionName\":\"FREE\", \"newSubscriptionName\":\"PREMIUM\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription with new subscriptionName " + request.getNewSubscriptionName() + " updated successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateSubscriptionName_subscriptionNotFound() throws Exception {
        UpdateSubscriptionNameDTO request = UpdateSubscriptionNameDTO.builder()
                .currentSubscriptionName("FREE")
                .newSubscriptionName("PREMIUM").build();

        doThrow(new NoSuchElementException("Subscription with subscriptionName " + request.getCurrentSubscriptionName() + " not found"))
                .when(subscriptionService).updateSubscriptionName(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/update_subscription_name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription with subscriptionName " + request.getCurrentSubscriptionName() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateSubscriptionPrice_success() throws Exception {
        UpdateSubscriptionPriceDTO request = UpdateSubscriptionPriceDTO.builder()
                .subscriptionName("FREE")
                .newPrice(BigDecimal.valueOf(350)).build();

        doNothing().when(subscriptionService).updateSubscriptionPrice(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/update_subscription_price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription with subscriptionName " + request.getSubscriptionName() + " has updated price"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateSubscriptionDurationTime_success() throws Exception {
        UpdateSubscriptionDurationTimeDTO request = UpdateSubscriptionDurationTimeDTO.builder()
                .subscriptionName("MAXIMUM")
                .newDurationTime(5).build();

        doNothing().when(subscriptionService).updateSubscriptionDurationTime(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/update_subscription_duration_time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription with subscriptionName " + request.getSubscriptionName() + " has updated duration time"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteSubscriptionById_success() throws Exception {
        long subscriptionId = 1L;
        doNothing().when(subscriptionService).deleteSubscriptionById(anyLong());

        mvc.perform(delete("/admin_office/delete_subscription_by_id")
                        .param("id", String.valueOf(subscriptionId)))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription with id " + subscriptionId + " delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteSubscriptionById_error_subscription_has_users() throws Exception {
        long subscriptionId = 1L;

        doThrow(new IllegalArgumentException("Subscription with id " + subscriptionId + " has users and cannot be deleted"))
                .when(subscriptionService).deleteSubscriptionById(subscriptionId);

        mvc.perform(delete("/admin_office/delete_subscription_by_id")
                        .param("id", String.valueOf(subscriptionId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Subscription with id " + subscriptionId + " has users and cannot be deleted"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteSubscriptionBySubscriptionName_success() throws Exception {
        String subscriptionName = "FREE";
        doNothing().when(subscriptionService).deleteSubscriptionBySubscriptionName(anyString());

        mvc.perform(delete("/admin_office/delete_subscription_by_subscription_name")
                        .param("subscriptionName", subscriptionName))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription with subscriptionName " + subscriptionName + " deleted successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteAllSubscriptions_success() throws Exception {
        String report = "Deleted subscriptions: FREE, OPTIMAL\nSubscription with subscriptionName ADMIN has users and cannot be deleted";
        when(subscriptionService.deleteAllSubscription()).thenReturn(report);

        mvc.perform(delete("/admin_office/delete_all_subscriptions"))
                .andExpect(status().isOk())
                .andExpect(content().string(report));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void addNewMusicFile_success() throws Exception {
        NewMusicFileDTO request = NewMusicFileDTO.builder()
                .musicFileName("Music").build();

        doNothing().when(musicFileService).addMusicFile(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(post("/admin_office/add_music_file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("New music file " + request.getMusicFileName() + " add in data base"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void addNewMusicFile_musicFileAlreadyExists() throws Exception {
        NewMusicFileDTO request = NewMusicFileDTO.builder()
                .musicFileName("Music").build();

        doThrow(new IllegalArgumentException("Music file with musicName " + request.getMusicFileName() + " already exists"))
                .when(musicFileService).addMusicFile(request);

        mvc.perform(post("/admin_office/add_music_file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Music file with musicName " + request.getMusicFileName() + " already exists"));

    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findAllMusicFiles_success() throws Exception {
        MusicFile firstMusicFile = MusicFile.builder()
                .musicFileName("firstMusicFile").build();
        MusicFile secondMusicFile = MusicFile.builder()
                .musicFileName("secondMusicFile").build();
        List<MusicFile> musicFiles = new ArrayList<>();
        musicFiles.add(firstMusicFile);
        musicFiles.add(secondMusicFile);

        when(musicFileService.findAllMusicFiles()).thenReturn(musicFiles);
        String musicFilesListJson = "[{\"musicFileName\":\"firstMusicFile\"},{\"musicFileName\":\"secondMusicFile\"}]";

        mvc.perform(get("/admin_office/all_music_files")
                        .content(musicFilesListJson))
                .andExpect(status().isOk())
                .andExpect(content().json(musicFilesListJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findAllMusicFiles_MusicFilesNotFound() throws Exception {
        doThrow(new NoSuchElementException("Any music files not found")).when(musicFileService).findAllMusicFiles();

        mvc.perform(get("/admin_office/all_music_files"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Any music files not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findMusicFileById_success() throws Exception {
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("musicFile").build();
        long musicFileId = 1L;
        when(musicFileService.findMusicFileById(musicFileId)).thenReturn(musicFile);
        String musicFileJson = "{\"musicFileName\":\"musicFile\"}";

        mvc.perform(get("/admin_office/music_file_by_id")
                        .param("id", String.valueOf(musicFileId)))
                .andExpect(status().isOk())
                .andExpect(content().json(musicFileJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findMusicFileById_musicFileNotFound() throws Exception {
        long musicFileId = 1L;
        doThrow(new NoSuchElementException("Music file with id " + musicFileId + " not found"))
                .when(musicFileService).findMusicFileById(musicFileId);

        mvc.perform(get("/admin_office/music_file_by_id")
                        .param("id", String.valueOf(musicFileId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Music file with id " + musicFileId + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findMusicFileByName_success() throws Exception {
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("musicFile").build();
        String findMusicFile = "musicFile";

        when(musicFileService.findMusicFileByMusicFileName(findMusicFile)).thenReturn(musicFile);
        String musicFileJson = "{\"musicFileName\":\"musicFile\"}";

        mvc.perform(get("/admin_office/music_file_by_name")
                        .param("name", findMusicFile))
                .andExpect(status().isOk())
                .andExpect(content().json(musicFileJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findMusicFileByName_musicFileNotFound() throws Exception {
        String musicFileName = "Music";
        doThrow(new NoSuchElementException("Music file with musicFileName " + musicFileName + " not found"))
                .when(musicFileService).findMusicFileByMusicFileName(musicFileName);

        mvc.perform(get("/admin_office/music_file_by_name")
                        .param("name", musicFileName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Music file with musicFileName " + musicFileName + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findMusicFilesBySubscription_success() throws Exception {
        Subscription subscription = Subscription.builder()
                .subscriptionName("FREE").build();
        MusicFile musicFile = MusicFile.builder()
                .musicFileName("Music").build();
        List<MusicFile> musicFiles = new ArrayList<>();
        musicFiles.add(musicFile);


        when(musicFileService.findMusicFilesBySubscription(subscription.getSubscriptionName()))
                .thenReturn(musicFiles);
        String musicFileJson = "[{\"musicFileName\":\"Music\"}]";

        mvc.perform(get("/admin_office/music_files_by_subscription")
                        .param("subscription_name", subscription.getSubscriptionName()))
                .andExpect(status().isOk())
                .andExpect(content().json(musicFileJson));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findMusicFilesBySubscription_subscriptionNotFound() throws Exception {
        String subscriptionName = "PREMIUM";

        doThrow(new NoSuchElementException("Subscription with name " + subscriptionName + " not found"))
                .when(musicFileService).findMusicFilesBySubscription(subscriptionName);

        mvc.perform(get("/admin_office/music_files_by_subscription")
                        .param("subscription_name", subscriptionName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription with name " + subscriptionName + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void findMusicFilesBySubscription_musicFilesNotFound() throws Exception {
        String subscriptionName = "FREE";

        doThrow(new NoSuchElementException("Subscription with name " + subscriptionName + " has no files"))
                .when(musicFileService).findMusicFilesBySubscription(subscriptionName);

        mvc.perform(get("/admin_office/music_files_by_subscription")
                        .param("subscription_name", subscriptionName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription with name " + subscriptionName + " has no files"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateMusicFileName_success() throws Exception {
        UpdateMusicFileNameDTO request = UpdateMusicFileNameDTO.builder()
                .currentMusicFileName("Music")
                .newMusicFileName("New_Music").build();

        doNothing().when(musicFileService).updateMusicFileName(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/update_music_file_name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("The music file named " + request.getCurrentMusicFileName() + " has been renamed to " + request.getNewMusicFileName()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateMusicFileName_musicFileNotFound() throws Exception {
        UpdateMusicFileNameDTO request = UpdateMusicFileNameDTO.builder()
                .currentMusicFileName("Music")
                .newMusicFileName("New_Music").build();

        doThrow(new NoSuchElementException("Music file with name " + request.getCurrentMusicFileName() + " not found"))
                .when(musicFileService).updateMusicFileName(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/update_music_file_name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Music file with name " + request.getCurrentMusicFileName() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateMusicFileName_newMusicFileNameAlreadyExists() throws Exception {
        UpdateMusicFileNameDTO request = UpdateMusicFileNameDTO.builder()
                .currentMusicFileName("Music")
                .newMusicFileName("New_Music").build();

        doThrow(new IllegalArgumentException("This music name " + request.getNewMusicFileName() + " already exists"))
                .when(musicFileService).updateMusicFileName(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/update_music_file_name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This music name " + request.getNewMusicFileName() + " already exists"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateMusicFileSubscription_success() throws Exception {
        UpdateMusicFileSubscriptionDTO request = UpdateMusicFileSubscriptionDTO.builder()
                .musicFileName("Music")
                .newMusicFileSubscription("OPTIMAL").build();

        doNothing().when(musicFileService).updateMusicFileSubscription(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/update_music_file_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Music file with name " + request.getMusicFileName() + " has received a new subscription " + request.getNewMusicFileSubscription()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateMusicFileSubscription_musicFileNotFound() throws Exception {
        UpdateMusicFileSubscriptionDTO request = UpdateMusicFileSubscriptionDTO.builder()
                .musicFileName("Music")
                .newMusicFileSubscription("OPTIMAL").build();

        doThrow(new NoSuchElementException("Music file with name " + request.getMusicFileName() + " not found"))
                .when(musicFileService).updateMusicFileSubscription(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/update_music_file_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Music file with name " + request.getMusicFileName() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateMusicFileSubscription_subscriptionFileNotFound() throws Exception {
        UpdateMusicFileSubscriptionDTO request = UpdateMusicFileSubscriptionDTO.builder()
                .musicFileName("Music")
                .newMusicFileSubscription("OPTIMAL").build();

        doThrow(new NoSuchElementException("Subscription with subscriptionName " + request.getNewMusicFileSubscription() + " not found"))
                .when(musicFileService).updateMusicFileSubscription(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/update_music_file_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription with subscriptionName " + request.getNewMusicFileSubscription() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void transferMusicFilesToAnotherSubscription_success() throws Exception {
        TransferMusicFilesToAnotherSubscription request = TransferMusicFilesToAnotherSubscription.builder()
                .currentMusicFilesSubscription("FREE")
                .newMusicFileSubscription("MAXIMUM").build();

        doNothing().when(musicFileService).transferMusicFilesToAnotherSubscription(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/transfer_music_files_to_another_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("All music files have been successfully transferred from the " + request.getCurrentMusicFilesSubscription() +
                        " subscription to the " + request.getNewMusicFileSubscription() + " subscription"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void transferMusicFilesToAnotherSubscription_subscriptionNotFound() throws Exception {
        TransferMusicFilesToAnotherSubscription request = TransferMusicFilesToAnotherSubscription.builder()
                .currentMusicFilesSubscription("FREE")
                .newMusicFileSubscription("MAXIMUM").build();

        doThrow(new NoSuchElementException("Subscription with subscriptionName " + request.getNewMusicFileSubscription() + " not found"))
                .when(musicFileService).transferMusicFilesToAnotherSubscription(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(put("/admin_office/transfer_music_files_to_another_subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription with subscriptionName " + request.getNewMusicFileSubscription() + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteAllMusicFiles_success() throws Exception {
        doNothing().when(musicFileService).deleteAllMusicFiles();

        mvc.perform(delete("/admin_office/delete_all_music_files"))
                .andExpect(status().isOk())
                .andExpect(content().string("All music files delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteAllMusicFiles_musicFilesNotFound() throws Exception {
        doThrow(new NoSuchElementException("Any music files not found"))
                .when(musicFileService).deleteAllMusicFiles();

        mvc.perform(delete("/admin_office/delete_all_music_files"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Any music files not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteMusicFilesByMusicFileName_success() throws Exception {
        String musicFileName = "Music";
        doNothing().when(musicFileService).deleteMusicFilesByMusicFileName(musicFileName);

        mvc.perform(delete("/admin_office/delete_music_file_by_music_file_name")
                        .param("musicFileName", musicFileName))
                .andExpect(status().isOk())
                .andExpect(content().string("Music file with name " + musicFileName + " delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteMusicFilesByMusicFileName_musicFileNotFound() throws Exception {
        String musicFileName = "Music";
        doThrow(new NoSuchElementException("Music file with name " + musicFileName + " not found"))
                .when(musicFileService).deleteMusicFilesByMusicFileName(musicFileName);

        mvc.perform(delete("/admin_office/delete_music_file_by_music_file_name")
                        .param("musicFileName", musicFileName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Music file with name " + musicFileName + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteMusicFilesById_success() throws Exception {
        long musicFileId = 1L;
        doNothing().when(musicFileService).deleteMusicFilesById(musicFileId);

        mvc.perform(delete("/admin_office/delete_music_file_by_id")
                        .param("musicFileId", String.valueOf(musicFileId)))
                .andExpect(status().isOk())
                .andExpect(content().string("Music file with id " + musicFileId + " delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteMusicFilesById_musicFileNotFound() throws Exception {
        long musicFileId = 1L;
        doThrow(new NoSuchElementException("Music file with id " + musicFileId + " not found"))
                .when(musicFileService).deleteMusicFilesById(musicFileId);

        mvc.perform(delete("/admin_office/delete_music_file_by_id")
                        .param("musicFileId", String.valueOf(musicFileId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Music file with id " + musicFileId + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteMusicFilesBySubscription_success() throws Exception {
        String subscriptionName = "FREE";

        doNothing().when(musicFileService).deleteMusicFilesBySubscription(subscriptionName);

        mvc.perform(delete("/admin_office/delete_music_files_by_subscription")
                        .param("subscription", subscriptionName))
                .andExpect(status().isOk())
                .andExpect(content().string("Music file with subscription " + subscriptionName + " delete successful"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteMusicFilesBySubscription_subscriptionNotFound() throws Exception {
        String subscriptionName = "FREE";

        doThrow(new NoSuchElementException("Subscription with name " + subscriptionName + " not found"))
                .when(musicFileService).deleteMusicFilesBySubscription(subscriptionName);

        mvc.perform(delete("/admin_office/delete_music_files_by_subscription")
                        .param("subscription", subscriptionName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Subscription with name " + subscriptionName + " not found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void addBankTransactionData_success() throws Exception {
        NewBankTransactionDataDTO request = NewBankTransactionDataDTO.builder()
                .bankName("SimpleBank")
                .bankUrlTransaction("simpleUrl").build();

        doNothing().when(bankTransactionDataService).addBankTransactionData(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(post("/admin_office/add_new_bank_transactional_data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("New transactional data successful added in database"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void addBankTransactionData_bankDataAlreadyExists() throws Exception {
        NewBankTransactionDataDTO request = NewBankTransactionDataDTO.builder()
                .bankName("SimpleBank")
                .bankUrlTransaction("simpleUrl").build();

        doThrow(new IllegalArgumentException("Bank with bank's name " + request.getBankName() + " already exists"))
                .when(bankTransactionDataService).addBankTransactionData(request);
        String requestJson = objectMapper.writeValueAsString(request);

        mvc.perform(post("/admin_office/add_new_bank_transactional_data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bank with bank's name " + request.getBankName() + " already exists"));
    }
}