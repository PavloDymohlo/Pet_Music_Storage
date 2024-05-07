package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.PetMusicStorage.dto.UpdatePhoneNumberDTO;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/personal_office")
public class PersonalOfficeController {
    private final UserService userService;
    private final JWTService jwtService;
    private final DatabaseUserDetailsService databaseUserDetailsService;


    @PutMapping("/updatePhoneNumber")
    public ResponseEntity<String> updatePhoneNumber(@RequestBody UpdatePhoneNumberDTO request,
                                                    @RequestHeader("Authorization") String jwtToken) {
        long currentUserPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
        log.debug("Current user's phone number retrieved: {}", currentUserPhoneNumber);
        try {
            userService.updatePhoneNumber(currentUserPhoneNumber, request.getNewPhoneNumber());
            UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getNewPhoneNumber()));
            String newJwtToken = jwtService.generateJwtToken(userDetails);
            log.info("Phone number updated successfully!");
            return ResponseEntity.ok().body(newJwtToken);
        } catch (IllegalArgumentException e) {
            log.warn("User with current phone number not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with current phone number not found");
        } catch (Exception e) {
            log.error("An error occurred while updating phone number", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating phone number");
        }
    }
}
