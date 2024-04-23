package ua.dymohlo.PetMusicStorage.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.AuthorityUtils;
import ua.dymohlo.PetMusicStorage.entity.Subscription;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class DatabaseUserDetailsServiceTest {
    @InjectMocks
    private DatabaseUserDetailsService databaseUserDetailsService;
    @Mock
    private UserRepository userRepository;

    @Test
    public void loadUserByUsername_userExists_returnsUserDetails() {
        long phoneNumber = 80661234569L;
        Subscription mockSubscription = new Subscription();
        mockSubscription.setSubscriptionName("MAXIMUM");
        User mockUser = new User();
        mockUser.setPhoneNumber(phoneNumber);
        mockUser.setPassword("password");
        mockUser.setSubscription(mockSubscription);
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(mockUser);

        UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(String.valueOf(phoneNumber));

        assertNotNull(userDetails);
        assertEquals(String.valueOf(phoneNumber), userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_MAXIMUM");
        List<GrantedAuthority> actualAuthorities = new ArrayList<>(userDetails.getAuthorities());
        assertEquals(authorities, actualAuthorities);

    }

    @Test
    public void loadUserByUsername_userDoesNotExist_throwsUsernameNotFoundException() {
        long phoneNumber = 80661234569L;
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            databaseUserDetailsService.loadUserByUsername(String.valueOf(phoneNumber));
        });
    }
}
