package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ua.dymohlo.PetMusicStorage.dto.*;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.service.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin_office")
public class AdminOfficeController {
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final MusicFileService musicFileService;
    private final BankTransactionDataService bankTransactionDataService;

    @GetMapping
    public ModelAndView adminOfficePage() {
        return new ModelAndView("pages/admin_office");
    }

    @DeleteMapping("/delete_user_by_id")
    public ResponseEntity<String> deleteUserById(@RequestParam("id") long userId) {
        try {
            userService.deleteUserById(userId);
            log.info("User with id :{} deleted successful", userId);
            String responseMessage = "User with id " + userId + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.error("User with id {} not found: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting a user by id", e);
            String errorMessage = "Error deleting user with id " + userId;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @DeleteMapping("/delete_users_by_bank_card_number")
    public ResponseEntity<String> deleteUsersByBankCardNumber(@RequestParam("bankCardNumber") long bankCardNumber,
                                                              @RequestHeader("Authorization") String jwtToken) {
        try {
            long currentAdminPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
            userService.deleteUserByBankCardNumber(bankCardNumber, currentAdminPhoneNumber);
            log.info("Users with bankCardNumber {} delete successful", bankCardNumber);
            String responseMessage = "Users with bankCardNumber " + bankCardNumber + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting users by bank card number", e);
            String errorMessage = "Error deleting users with bank card number " + bankCardNumber;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @DeleteMapping("/delete_users_by_subscription")
    public ResponseEntity<String> deleteUsersBySubscription(@RequestParam("subscription") String subscription,
                                                            @RequestHeader("Authorization") String jwtToken) {
        try {
            long currentAdminPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
            userService.deleteUsersBySubscription(currentAdminPhoneNumber, subscription);
            log.info("Users with subscription {} delete successful", subscription);
            String responseMessage = "Users with subscription " + subscription + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting users by subscription ", e);
            String errorMessage = "Error deleting users with subscription " + subscription;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @DeleteMapping("/delete_user_by_email")
    public ResponseEntity<String> deleteUserByEmail(@RequestParam("email") String userEmail) {
        try {
            userService.deleteUserByEmail(userEmail);
            log.info("User with email {} delete successful", userEmail);
            String responseMessage = "User with email " + userEmail + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting user by email ", e);
            String errorMessage = "Error deleting user with email " + userEmail;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @GetMapping("/subscription_by_id")
    public ResponseEntity<Object> findSubscriptionById(@RequestParam("id") long subscriptionId) {
        try {
            Subscription subscription = subscriptionService.findSubscriptionById(subscriptionId);
            log.info("fetched subscription by id successful");
            return ResponseEntity.ok(subscription);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding subscription by id");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> findAllSubscriptions() {
        try {
            List<Subscription> subscriptions = subscriptionService.findAllSubscription();
            log.info("Fetched all subscription");
            return ResponseEntity.ok(subscriptions);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding all subscriptions");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/subscription_by_price")
    public ResponseEntity<?> findSubscriptionsByPrice(@RequestBody FindSubscriptionsByPriceDTO request) {
        try {
            List<Subscription> subscriptions = subscriptionService.findSubscriptionsByPrice(request.getMinPrice(),
                    request.getMaxPrice());
            log.info("Fetched subscriptions by price");
            return ResponseEntity.ok(subscriptions);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding subscription by price");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/subscription_by_name")
    public ResponseEntity<Object> findSubscriptionBySubscriptionName(@RequestParam("subscriptionName") String subscriptionName) {
        try {
            Subscription subscription = subscriptionService.findSubscriptionBySubscriptionName(subscriptionName);
            log.info("Fetched subscription by subscriptionName");
            return ResponseEntity.ok(subscription);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding subscription by subscriptionName");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update_subscription_name")
    public ResponseEntity<String> updateSubscriptionName(@RequestBody UpdateSubscriptionNameDTO request) {
        try {
            subscriptionService.updateSubscriptionName(request);
            log.info("Subscription with new subscriptionName {} updated successful", request.getNewSubscriptionName());
            String responseMessage = "Subscription with new subscriptionName " + request.getNewSubscriptionName() + " updated successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while subscriptionName updated");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_subscription_price")
    public ResponseEntity<String> updateSubscriptionPrice(@RequestBody UpdateSubscriptionPriceDTO request) {
        try {
            subscriptionService.updateSubscriptionPrice(request);
            log.info("Subscription with subscriptionName {} has updated price", request.getSubscriptionName());
            String responseMessage = "Subscription with subscriptionName " + request.getSubscriptionName() + " has updated price";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while subscriptionPrice updated");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update_subscription_duration_time")
    public ResponseEntity<String> updateSubscriptionDurationTime(@RequestBody UpdateSubscriptionDurationTimeDTO request) {
        try {
            subscriptionService.updateSubscriptionDurationTime(request);
            log.info("Subscription with subscriptionName {} has updated duration time", request.getSubscriptionName());
            String responseMessage = "Subscription with subscriptionName " + request.getSubscriptionName() + " has updated duration time";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while subscription duration time updated");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete_subscription_by_id")
    public ResponseEntity<String> deleteSubscriptionById(@RequestParam("id") long subscriptionId) {
        try {
            subscriptionService.deleteSubscriptionById(subscriptionId);
            log.info("Subscription with id {} delete successful", subscriptionId);
            String responseMessage = "Subscription with id " + subscriptionId + " delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while deleting subscription by id");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete_subscription_by_subscription_name")
    public ResponseEntity<String> deleteSubscriptionBySubscriptionName(@RequestParam("subscriptionName") String subscriptionName) {
        try {
            subscriptionService.deleteSubscriptionBySubscriptionName(subscriptionName);
            log.info("Subscription with subscriptionName {} deleted successful", subscriptionName);
            String responseMessage = "Subscription with subscriptionName " + subscriptionName + " deleted successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while deleting subscription by subscriptionName");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete_all_subscriptions")
    public ResponseEntity<String> deleteAllSubscriptions() {
        try {
            String report = subscriptionService.deleteAllSubscription();
            log.info(report);
            return ResponseEntity.ok(report);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while deleting all subscriptions");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add_music_file")
    public ResponseEntity<String> addNewMusicFile(@RequestBody NewMusicFileDTO request) {
        try {
            musicFileService.addMusicFile(request);
            log.info("New music file {} add in data base", request.getMusicFileName());
            String responseMessage = "New music file " + request.getMusicFileName() + " add in data base";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding new music file");
            String errorMessage = "Error adding new music file " + request.getMusicFileName();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @GetMapping("/all_music_files")
    public ResponseEntity<?> findAllMusicFiles() {
        try {
            List<MusicFile> musicFiles = musicFileService.findAllMusicFiles();
            log.info("All music files found successful");
            return ResponseEntity.ok(musicFiles);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding all music files");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/music_file_by_id")
    public ResponseEntity<Object> findMusicFileById(@RequestParam("id") long musicFileId) {
        try {
            MusicFile musicFile = musicFileService.findMusicFileById(musicFileId);
            log.info("Music file by id {} found successful", musicFileId);
            return ResponseEntity.ok(musicFile);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding music file by id");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/music_file_by_name")
    public ResponseEntity<Object> findMusicFileByName(@RequestParam("name") String musicFileName) {
        try {
            MusicFile musicFile = musicFileService.findMusicFileByMusicFileName(musicFileName);
            log.info("Music file with name {} found successful", musicFileName);
            return ResponseEntity.ok(musicFile);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding music file by name");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/music_files_by_subscription")
    public ResponseEntity<?> findMusicFilesBySubscription(@RequestParam("subscription_name") String subscriptionName) {
        try {
            List<MusicFile> musicFiles = musicFileService.findMusicFilesBySubscription(subscriptionName);
            log.info("Music file successful found by subscription {}", subscriptionName);
            return ResponseEntity.ok(musicFiles);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding music file by subscription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/update_music_file_name")
    public ResponseEntity<String> updateMusicFileName(@RequestBody UpdateMusicFileNameDTO request) {
        try {
            musicFileService.updateMusicFileName(request);
            log.info("The music file named {}  has been renamed to {}", request.getCurrentMusicFileName(), request.getNewMusicFileName());
            String responseMessage = "The music file named " + request.getCurrentMusicFileName() + " has been renamed to " + request.getNewMusicFileName();
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/update_music_file_subscription")
    public ResponseEntity<String> updateMusicFileSubscription(@RequestBody UpdateMusicFileSubscriptionDTO request) {
        try {
            musicFileService.updateMusicFileSubscription(request);
            log.info("Music file with name {} has received a new subscription {}", request.getMusicFileName(), request.getNewMusicFileSubscription());
            String responseMessage = "Music file with name " + request.getMusicFileName() + " has received a new subscription " + request.getNewMusicFileSubscription();
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/transfer_music_files_to_another_subscription")
    public ResponseEntity<String> transferMusicFilesToAnotherSubscription(@RequestBody TransferMusicFilesToAnotherSubscription request) {
        try {
            musicFileService.transferMusicFilesToAnotherSubscription(request);
            log.info("All music files have been successfully transferred from the {} subscription to the {} subscription",
                    request.getCurrentMusicFilesSubscription(), request.getNewMusicFileSubscription());
            String responseMessage = "All music files have been successfully transferred from the " + request.getCurrentMusicFilesSubscription() +
                    " subscription to the " + request.getNewMusicFileSubscription() + " subscription";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete_all_music_files")
    public ResponseEntity<String> deleteAllMusicFiles() {
        try {
            musicFileService.deleteAllMusicFiles();
            log.info("All music files delete successful");
            String responseMessage = "All music files delete successful";
            return ResponseEntity.ok(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete_music_file_by_music_file_name")
    public ResponseEntity<String> deleteMusicFilesByMusicFileName(@RequestParam("musicFileName") String musicFileName) {
        try {
            musicFileService.deleteMusicFilesByMusicFileName(musicFileName);
            log.info("Music file with name {} delete successful", musicFileName);
            String responseMessage = "Music file with name " + musicFileName + " delete successful";
            return ResponseEntity.ok().body(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete_music_file_by_id")
    public ResponseEntity<String> deleteMusicFilesById(@RequestParam("musicFileId") long musicFileId) {
        try {
            musicFileService.deleteMusicFilesById(musicFileId);
            log.info("Music file with id {} delete successful", musicFileId);
            String responseMessage = "Music file with id " + musicFileId + " delete successful";
            return ResponseEntity.ok().body(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete_music_files_by_subscription")
    public ResponseEntity<String> deleteMusicFilesBySubscription(@RequestParam("subscription") String subscription) {
        try {
            musicFileService.deleteMusicFilesBySubscription(subscription);
            log.info("Music files with subscription {} delete successful", subscription);
            String responseMessage = "Music file with subscription " + subscription + " delete successful";
            return ResponseEntity.ok().body(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/add_new_bank_transactional_data")
    public ResponseEntity<Object> addBankTransactionData(@RequestBody NewBankTransactionDataDTO request) {
        try {
            bankTransactionDataService.addBankTransactionData(request);
            log.info("New transactional data successful added in database");
            String responseMessage = "New transactional data successful added in database";
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}