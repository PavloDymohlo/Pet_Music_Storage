package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/update_cookie")
public class UpdateCookieController {
    private final UserService userService;
    private final DatabaseUserDetailsService databaseUserDetailsService;
    private final JWTService jwtService;

    @PostMapping
    public ResponseEntity<String> updateCookie(@RequestHeader("Authorization") String jwtToken,
                                               HttpServletResponse response) {
        try {
            long userPhoneNumber = userService.getCurrentUserPhoneNumber(jwtToken);
            User user = userService.findUserByPhoneNumber(userPhoneNumber);
            log.debug("Current user's phone number retrieved: {}", userPhoneNumber);
            UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(user.getPhoneNumber()));
            String newJwtToken = jwtService.generateJwtToken(userDetails);
            String responseMessage = "JWT token successfully added to cookies";
            ResponseCookie jwtCookie = ResponseCookie.from("JWT_TOKEN", newJwtToken)
                    .httpOnly(false)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofHours(1))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
            return ResponseEntity.ok().body(responseMessage);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}