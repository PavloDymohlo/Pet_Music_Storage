package ua.dymohlo.PetMusicStorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import ua.dymohlo.PetMusicStorage.dto.UpdatePhoneNumberDTO;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PersonalOfficeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class PersonalOfficeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService mockUserService;
    @MockBean
    private JWTService mockJwtService;
    @MockBean
    private DatabaseUserDetailsService mockDatabaseUserDetailsService;
    @MockBean
    private UserDetails mockUserDetails;
    @MockBean
    private UserRepository mockUserRepository;

    @Test
    public void updatePhoneNumber_success() throws Exception {
        UpdatePhoneNumberDTO updatePhoneNumberDTO = new UpdatePhoneNumberDTO();
        updatePhoneNumberDTO.setNewPhoneNumber(80663256655L);
        String jwtToken = "mockJWTToken";
        UserDetails userDetails = mock(UserDetails.class);
        when(mockDatabaseUserDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(mockUserService.getCurrentUserPhoneNumber(anyString())).thenReturn(80663256699l);
        when(mockJwtService.generateJwtToken(any(UserDetails.class))).thenReturn("newJWTToken");

        mockMvc.perform(put("/personal_office/updatePhoneNumber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(new ObjectMapper().writeValueAsString(updatePhoneNumberDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("newJWTToken"));

        verify(mockUserService, times(1)).getCurrentUserPhoneNumber(jwtToken);
        verify(mockUserService, times(1)).updatePhoneNumber(80663256699l, 80663256655L);
        verify(mockDatabaseUserDetailsService, times(1)).loadUserByUsername("80663256655");
        verify(mockJwtService, times(1)).generateJwtToken(userDetails);
    }
    @Test
    public void updatePhoneNumber_phoneNumberAlreadyExists() throws Exception {
        UpdatePhoneNumberDTO updatePhoneNumberDTO = new UpdatePhoneNumberDTO();
        updatePhoneNumberDTO.setNewPhoneNumber(80663256655L);
        String jwtToken = "mockJWTToken";
        when(mockUserService.getCurrentUserPhoneNumber(anyString())).thenReturn(80663256699L);
        doThrow(new IllegalArgumentException("Phone number already exists")).when(mockUserService).updatePhoneNumber(80663256699L, 80663256655L);

        mockMvc.perform(put("/personal_office/updatePhoneNumber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(new ObjectMapper().writeValueAsString(updatePhoneNumberDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Phone number already exists"));

        verify(mockUserService, times(1)).getCurrentUserPhoneNumber(jwtToken);
        verify(mockUserService, times(1)).updatePhoneNumber(80663256699L, 80663256655L); // verifies service method call
        verifyNoInteractions(mockDatabaseUserDetailsService, mockJwtService);
    }



}
