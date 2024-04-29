package ua.dymohlo.PetMusicStorage.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;
import org.springframework.security.core.userdetails.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTTokenConfigTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private JWTTokenConfig jwtTokenConfig;

    @Test
    void testDoFilterInternal_WithToken() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        UserDetails userDetails = new User("username", "password", Collections.emptyList());
        String token = "dummyToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUserName(anyString())).thenReturn("username");
        UserDetailsService userDetailsServiceMock = mock(UserDetailsService.class);
        when(userDetailsServiceMock.loadUserByUsername("username")).thenReturn(userDetails);
        when(userService.userDetailsService()).thenReturn(userDetailsServiceMock);
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);

        jwtTokenConfig.doFilterInternal(request, response, filterChain);

        verify(userService, times(1)).userDetailsService();
        verify(jwtService, times(1)).isTokenValid(anyString(), any(UserDetails.class));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoToken() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn(null);

        jwtTokenConfig.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

}
