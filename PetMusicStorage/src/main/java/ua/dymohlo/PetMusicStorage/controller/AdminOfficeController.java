package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.PetMusicStorage.dto.UpdatePhoneNumberDTO;
import ua.dymohlo.PetMusicStorage.dto.UpdateUserBankCardDTO;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin_office")
public class AdminOfficeController {
    private final UserService userService;
    private final JWTService jwtService;
    private final DatabaseUserDetailsService databaseUserDetailsService;


    @PutMapping("/updatePhoneNumber")
    public ResponseEntity<String> updateUserPhoneNumber(@RequestBody UpdatePhoneNumberDTO request) {
        try {
            userService.updatePhoneNumber(request.getCurrentPhoneNumber(), request.getNewPhoneNumber());
            UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getNewPhoneNumber()));
            String newJwtToken = jwtService.generateJwtToken(userDetails);
            log.info("Phone number updated successfully!");
            return ResponseEntity.ok().body(newJwtToken);
        } catch (IllegalArgumentException e) {
            log.warn("Phone number already exists");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating phone number", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updateBankCard")
    public ResponseEntity<String> updateUserBankCard(@RequestBody UpdateUserBankCardDTO request) {
        try {
            userService.updateBankCard(request.getUserPhoneNumber(), request);
            return ResponseEntity.ok("Bank card updated successful");
        } catch (IllegalArgumentException e) {
            log.warn("Invalid card details");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while updating bank card");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
