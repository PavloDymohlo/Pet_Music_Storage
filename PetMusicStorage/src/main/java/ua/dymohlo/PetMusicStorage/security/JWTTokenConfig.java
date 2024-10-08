package ua.dymohlo.PetMusicStorage.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ua.dymohlo.PetMusicStorage.service.JWTService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;


@Component
@RequiredArgsConstructor
@Slf4j
public class JWTTokenConfig extends OncePerRequestFilter {
    private final JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Filter starts");
        if ("/host_page".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String authHeader = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("JWT_TOKEN"))
                    .findAny()
                    .map(c -> "Bearer " + c.getValue())
                    .orElse(null);
            log.info("Jwt Token: " + authHeader);
            String username = null;
            String jwt = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                log.info("Received JWT token: {}", jwt);
                try {
                    username = jwtService.extractUserName(jwt);
                } catch (ExpiredJwtException e) {
                    log.debug("Token lifetime has expired");
                } catch (SignatureException e) {
                    log.debug("The signature is incorrect");
                } catch (Exception e) {
                    log.debug("Error parsing token: " + e.getMessage());
                }
            }
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<GrantedAuthority> authorities = jwtService.getRoles(jwt).stream()
                        .map(role -> new SimpleGrantedAuthority(role))
                        .collect(Collectors.toList());
                log.info("User {} with roles {}", username, authorities);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}