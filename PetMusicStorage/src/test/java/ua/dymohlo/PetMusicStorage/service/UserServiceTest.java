package ua.dymohlo.PetMusicStorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DatabaseUserDetailsService databaseUserDetailsService;
    @Mock
    private UserBankCard userBankCard;

    @BeforeEach
    public void setUp() {
        long phoneNumber = 809700012345L;
        UserBankCard mockUserBankCard = UserBankCard.builder()
                .cardNumber(1234_5678_9012_3456L)
                .cardExpirationDate("25/25")
                .cvv((short) 111).build();
        String mockEmail = "mockUser@gmail.com";
        String password = "password";
    }

    @Test
    public void userDetailsService_PhoneNumberExist() {
        long phoneNumber = 80996663322L;
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(databaseUserDetailsService.loadUserByUsername(String.valueOf(phoneNumber))).thenReturn(mockUserDetails);

        UserDetailsService userDetailsService = userService.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(phoneNumber));

        assertNotNull(userDetails);
        assertEquals(mockUserDetails, userDetails);
        verify(databaseUserDetailsService).loadUserByUsername(String.valueOf(phoneNumber));
    }

    @Test
    public void userDetailService_PhoneNumberNotFound() {
        long phoneNumber = 80996663322L;

        UserDetailsService userDetailsService = userService.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(phoneNumber));

        assertNull(userDetails);
    }

    @Test
    public void registerUser_Success() {

    }
}