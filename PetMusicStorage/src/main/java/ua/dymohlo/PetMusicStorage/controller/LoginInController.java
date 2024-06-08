package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.PetMusicStorage.dto.ErrorLoginDTO;
import ua.dymohlo.PetMusicStorage.dto.LoginResponseDTO;
import ua.dymohlo.PetMusicStorage.dto.UserLoginInDTO;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/login")
public class LoginInController {
    private final UserService userService;
    private final JWTService jwtService;
    private final DatabaseUserDetailsService databaseUserDetailsService;

    @PostMapping
    public ResponseEntity<?> loginIn(@RequestBody UserLoginInDTO request) {
        try {
            userService.loginIn(request);
            UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(request.getPhoneNumber()));
            String jwtToken = jwtService.generateJwtToken(userDetails);
            log.info("Generated JWT token: {}", jwtToken);
            String redirectUrl;
            if (userService.isAdminSubscription(request.getPhoneNumber())) {
                log.info("User with phone number {} redirected to admin_office page", request.getPhoneNumber());
                redirectUrl = "/admin_office";
            } else {
                log.info("User with phone number {} redirected to personal_office page", request.getPhoneNumber());
                redirectUrl = "/personal_office";
            }
            return ResponseEntity.ok().body(new LoginResponseDTO(redirectUrl, jwtToken));
        } catch (NoSuchElementException e) {
            log.error("Login attempt failed for user with phone number {}: {}", request.getPhoneNumber(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorLoginDTO(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Login attempt failed for user with phone number {}: {}", request.getPhoneNumber(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorLoginDTO(e.getMessage()));
        }
    }
}