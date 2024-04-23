package ua.dymohlo.PetMusicStorage.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ua.dymohlo.PetMusicStorage.entity.User;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JWTService {
    private final String jwtSigningKey;
    @Value("${jwt.expiration}")
    private int jwtExpirationMinutes;

    public JWTService() {
        jwtSigningKey = System.getenv("SECRET_KEY");
        if (jwtSigningKey == null) {
            throw new IllegalStateException("SECRET_KEY environment variable not found!");
        }
        log.info("Loaded SECRET_KEY from environment variable");
    }

    public String generateJwtToken(UserDetails userDetails) {
        log.debug("Generating JWT token for user:{}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User user) {
            claims.put("id", user.getId());
            claims.put("phone number", user.getPhoneNumber());
            claims.put("password", user.getPassword());
        }
        return createJwtToken(claims, userDetails);
    }

    private String createJwtToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        log.debug("Creating JWT token for user: {}", userDetails.getUsername());
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMinutes))
                .signWith(SignatureAlgorithm.HS256, getSigningKey()).compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String jwtToken, UserDetails userDetails) {
        log.debug("Validating JWT token for user: {}", userDetails.getUsername());
        final String userName = extractUserName(jwtToken);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(jwtToken));
    }

    public String extractUserName(String jwtToken) {
        log.debug("Extracting username from JWT token/");
        return extractClaim(jwtToken, claims -> claims.getSubject());
    }

    private <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolves) {
        final Claims claims = extractAllClaims(jwtToken);
        return claimsResolves.apply(claims);
    }

    private Claims extractAllClaims(String jwtToken) {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSigningKey))).build()
                .parseClaimsJws(jwtToken).getBody();
    }

    private boolean isTokenExpired(String jwtToken) {
        return extractExpiration(jwtToken).before(new Date());
    }

    private Date extractExpiration(String jwtToken) {
        return extractClaim(jwtToken, claims -> claims.getExpiration());
    }
}
