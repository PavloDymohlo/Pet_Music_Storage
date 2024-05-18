package ua.dymohlo.PetMusicStorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import ua.dymohlo.PetMusicStorage.Enum.AutoRenewStatus;
import ua.dymohlo.PetMusicStorage.dto.*;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;
import ua.dymohlo.PetMusicStorage.security.DatabaseUserDetailsService;
import ua.dymohlo.PetMusicStorage.service.JWTService;
import ua.dymohlo.PetMusicStorage.service.UserService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

//@WebMvcTest(controllers = AdminOfficeController.class)
//@AutoConfigureMockMvc
//@ExtendWith(MockitoExtension.class)
//public class AdminOfficeControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//    @MockBean
//    private UserService mockUserService;
//    @MockBean
//    private JWTService mockJwtService;
//    @MockBean
//    private DatabaseUserDetailsService mockDatabaseUserDetailsService;
//    @MockBean
//    private UserDetails mockUserDetails;
//    @MockBean
//    private UserRepository mockUserRepository;
//    @MockBean
//    LoginInController mockLoginInController;
//    @Autowired
//    private ObjectMapper objectMapper;

//    @Test
//    public void updateUserPhoneNumber_Success() {
//        AdminOfficeController controller = new AdminOfficeController(mockUserService, mockJwtService, mockDatabaseUserDetailsService);
//        doNothing().when(mockUserService).updatePhoneNumber(anyLong(), anyLong());
//        when(mockJwtService.generateJwtToken(any())).thenReturn("mockedJwtToken");
//        when(mockDatabaseUserDetailsService.loadUserByUsername(anyString())).thenReturn(mockUserDetails);
//
//        UpdatePhoneNumberDTO request = new UpdatePhoneNumberDTO(80996663999L, 80996663997L);
//        ResponseEntity<String> response = controller.updateUserPhoneNumber(request);
//
//        assert response.getStatusCode().equals(HttpStatus.OK);
//        assert response.getBody().equals("mockedJwtToken");
//    }
//
//    @Test
//    public void updateUserPhoneNumber_phoneNumberAlreadyExists() {
//        AdminOfficeController controller = new AdminOfficeController(mockUserService, mockJwtService, mockDatabaseUserDetailsService);
//        doThrow(new IllegalArgumentException("Phone number already exists")).when(mockUserService).updatePhoneNumber(anyLong(), anyLong());
//
//        UpdatePhoneNumberDTO request = new UpdatePhoneNumberDTO(80996663999L, 80996663997L);
//        ResponseEntity<String> response = controller.updateUserPhoneNumber(request);
//
//        assert response.getStatusCode().equals(HttpStatus.BAD_REQUEST);
//        assert response.getBody().equals("Phone number already exists");
//    }
//
//    @Test
//    public void updateBankCard_success() {
//        AdminOfficeController controller = new AdminOfficeController(mockUserService, mockJwtService, mockDatabaseUserDetailsService);
//        doNothing().when(mockUserService).updateBankCard(anyLong(), any());
//        when(mockJwtService.generateJwtToken(any())).thenReturn("mockJwtToken");
//        when(mockDatabaseUserDetailsService.loadUserByUsername(anyString())).thenReturn(mockUserDetails);
//        UpdateUserBankCardDTO request = UpdateUserBankCardDTO.builder()
//                .newUserBankCard(UserBankCard.builder()
//                        .cardNumber(1234567890123456L)
//                        .cardExpirationDate("25/25")
//                        .cvv((short) 123).build()).userPhoneNumber(80663256655L).build();
//
//        ResponseEntity<String> response = controller.updateUserBankCard(request);
//
//        assert response.getStatusCode().equals(HttpStatus.OK);
//        assert response.getBody().equals("Bank card updated successful");
//    }
//
//    @Test
//    public void updateBankCard_invalidCardDetails() {
//        AdminOfficeController controller = new AdminOfficeController(mockUserService, mockJwtService, mockDatabaseUserDetailsService);
//        doThrow(new IllegalArgumentException("Invalid card details")).when(mockUserService).updateBankCard(anyLong(), any());
//        UpdateUserBankCardDTO request = UpdateUserBankCardDTO.builder()
//                .newUserBankCard(UserBankCard.builder()
//                        .cardNumber(1234567890123456L)
//                        .cardExpirationDate("25/25")
//                        .cvv((short) 123).build()).userPhoneNumber(80663256655L).build();
//
//        ResponseEntity<String> response = controller.updateUserBankCard(request);
//
//        assert response.getStatusCode().equals(HttpStatus.BAD_REQUEST);
//        assert response.getBody().equals("Invalid card details");
//    }
//
//    @Test
//    public void updateUserPassword_success() {
//        AdminOfficeController controller = new AdminOfficeController(mockUserService, mockJwtService, mockDatabaseUserDetailsService);
//        doNothing().when(mockUserService).updatePassword(anyLong(), any());
//        when(mockJwtService.generateJwtToken(any())).thenReturn("mockJwtToken");
//        when(mockDatabaseUserDetailsService.loadUserByUsername(anyString())).thenReturn(mockUserDetails);
//        UpdatePasswordDTO updatePasswordDTO = UpdatePasswordDTO.builder()
//                .userPhoneNumber(80663256655L)
//                .newPassword("password").build();
//
//        ResponseEntity<String> response = controller.updateUserPassword(updatePasswordDTO);
//
//        assert response.getStatusCode().equals(HttpStatus.OK);
//        assert response.getBody().equals("Password updated successful");
//    }
//
//    @Test
//    public void updateUserPassword_phoneNumberNotFound() {
//        AdminOfficeController controller = new AdminOfficeController(mockUserService, mockJwtService, mockDatabaseUserDetailsService);
//        doThrow(new IllegalArgumentException("Phone number not found")).when(mockUserService).updatePassword(anyLong(), any());
//        UpdatePasswordDTO updatePasswordDTO = UpdatePasswordDTO.builder()
//                .userPhoneNumber(80663256655L)
//                .newPassword("password").build();
//
//        ResponseEntity<String> response = controller.updateUserPassword(updatePasswordDTO);
//
//        assert response.getStatusCode().equals(HttpStatus.BAD_REQUEST);
//        assert response.getBody().equals("Phone number not found");
//    }
//
//    @Test
//    public void updateUserEmail_success() {
//        AdminOfficeController controller = new AdminOfficeController(mockUserService, mockJwtService, mockDatabaseUserDetailsService);
//        doNothing().when(mockUserService).updateEmail(anyLong(), any());
//        when(mockJwtService.generateJwtToken(any())).thenReturn("mockJwtToken");
//        when(mockDatabaseUserDetailsService.loadUserByUsername(anyString())).thenReturn(mockUserDetails);
//        UpdateEmailDTO updateEmailDTO = UpdateEmailDTO.builder()
//                .userPhoneNumber(80663256655L)
//                .newEmail("newEmail@example.com").build();
//
//        ResponseEntity<String> response = controller.updateUserEmail(updateEmailDTO);
//
//        assert response.getStatusCode().equals(HttpStatus.OK);
//        assert response.getBody().equals("Email updated successful");
//    }
//
//    @Test
//    public void updateUserEmail_emailIsAlreadyExists() {
//        AdminOfficeController controller = new AdminOfficeController(mockUserService, mockJwtService, mockDatabaseUserDetailsService);
//        doThrow(new IllegalArgumentException("Email is already exists")).when(mockUserService).updateEmail(anyLong(), any());
//        UpdateEmailDTO updateEmailDTO = UpdateEmailDTO.builder()
//                .userPhoneNumber(80663256655L)
//                .newEmail("newEmail@example.com").build();
//
//        ResponseEntity<String> response = controller.updateUserEmail(updateEmailDTO);
//
//        assert response.getStatusCode().equals(HttpStatus.BAD_REQUEST);
//        assert response.getBody().equals("Email is already exists");
//    }
//
//    @Test
//    public void setUserAutoRenewStatus_success() {
//        AdminOfficeController controller = new AdminOfficeController(mockUserService, mockJwtService, mockDatabaseUserDetailsService);
//        doNothing().when(mockUserService).setAutoRenewStatus(anyLong(), any());
//        when(mockJwtService.generateJwtToken(any())).thenReturn("mockJwtToken");
//        when(mockDatabaseUserDetailsService.loadUserByUsername(any())).thenReturn(mockUserDetails);
//        SetAutoRenewDTO setAutoRenewDTO = SetAutoRenewDTO.builder()
//                .userPhoneNumber(80663256655L)
//                .autoRenewStatus(AutoRenewStatus.YES).build();
//
//        ResponseEntity<String> response = controller.setUserAutoRenewStatus(setAutoRenewDTO);
//
//        assert response.getStatusCode().equals(HttpStatus.OK);
//        assert response.getBody().equals("Auto renew status set successfully");
//    }
//}
