package ua.dymohlo.PetMusicStorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.*;
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
import ua.dymohlo.PetMusicStorage.dto.UpdateUserBankCardDTO;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;
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
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void updatePhoneNumber_success() throws Exception {
        UpdatePhoneNumberDTO updatePhoneNumberDTO = new UpdatePhoneNumberDTO();
        updatePhoneNumberDTO.setNewPhoneNumber(80663256655L);
        String jwtToken = "mockJWTToken";
        when(mockDatabaseUserDetailsService.loadUserByUsername(anyString())).thenReturn(mockUserDetails);
        when(mockUserService.getCurrentUserPhoneNumber(anyString())).thenReturn(80663256699l);
        when(mockJwtService.generateJwtToken(any(UserDetails.class))).thenReturn("newJWTToken");

        mockMvc.perform(put("/personal_office/updatePhoneNumber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(updatePhoneNumberDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("newJWTToken"));

        verify(mockUserService, times(1)).getCurrentUserPhoneNumber(jwtToken);
        verify(mockUserService, times(1)).updatePhoneNumber(80663256699l, 80663256655L);
        verify(mockDatabaseUserDetailsService, times(1)).loadUserByUsername("80663256655");
        verify(mockJwtService, times(1)).generateJwtToken(mockUserDetails);
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
                        .content(objectMapper.writeValueAsString(updatePhoneNumberDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Phone number already exists"));

        verify(mockUserService, times(1)).getCurrentUserPhoneNumber(jwtToken);
        verify(mockUserService, times(1)).updatePhoneNumber(80663256699L, 80663256655L); // verifies service method call
        verifyNoInteractions(mockDatabaseUserDetailsService, mockJwtService);
    }

    @Test
    public void updateBankCard_success() throws Exception {
        UpdateUserBankCardDTO updateUserBankCardDTO = UpdateUserBankCardDTO.builder()
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(1234567890123456L)
                        .cardExpirationDate("25/25")
                        .cvv((short) 123).build()).userPhoneNumber(80663256655L).build();
        String jwtToken = "mockJWTToken";
        when(mockUserService.getCurrentUserPhoneNumber(anyString())).thenReturn(80663256655L);
        doNothing().when(mockUserService).updateBankCard(anyLong(), any(UpdateUserBankCardDTO.class));

        mockMvc.perform(put("/personal_office/updateBankCard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(updateUserBankCardDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bank card updated successful"));

        verify(mockUserService, times(1)).updateBankCard(anyLong(), any(UpdateUserBankCardDTO.class));
    }
    @Test
    public void updateBankCard_invalidCardDetails() throws Exception {
        UpdateUserBankCardDTO updateUserBankCardDTO = UpdateUserBankCardDTO.builder()
                .newUserBankCard(UserBankCard.builder()
                        .cardNumber(1234567890123456L)
                        .cardExpirationDate("25/25")
                        .cvv((short) 123).build()).userPhoneNumber(80663256655L).build();
        String jwtToken = "mockJWTToken";
        when(mockUserService.getCurrentUserPhoneNumber(anyString())).thenReturn(80663256655L);
        doThrow(new IllegalArgumentException("Invalid card details")).when(mockUserService).updateBankCard(anyLong(), any(UpdateUserBankCardDTO.class));

        mockMvc.perform(put("/personal_office/updateBankCard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwtToken)
                        .content(objectMapper.writeValueAsString(updateUserBankCardDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid card details"));

        verify(mockUserService, times(1)).updateBankCard(anyLong(), any(UpdateUserBankCardDTO.class));
    }

}