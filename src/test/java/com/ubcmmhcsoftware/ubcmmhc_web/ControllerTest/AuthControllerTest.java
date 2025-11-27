package com.ubcmmhcsoftware.ubcmmhc_web.ControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Controller.AuthController;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.ResetPasswordDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthResponsiveService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RestClientTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthResponsiveService authResponsiveService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterUser() throws Exception {
        LoginDTO loginDTO = new LoginDTO();

        doNothing().when(authService).registerUser(any(LoginDTO.class));

        mockMvc.perform(post("/api/auth/register-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());

        verify(authService, times(1)).registerUser(any(LoginDTO.class));
    }

    @Test
    void testLoginUser() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        CustomUserDetails mockUser = new CustomUserDetails(null);

        when(authService.loginUser(any(LoginDTO.class))).thenReturn(mockUser);

        doNothing().when(authResponsiveService).handleSuccessfulAuthentication(any(HttpServletResponse.class), eq(mockUser), any());

        mockMvc.perform(post("/api/auth/login-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());

        verify(authService, times(1)).loginUser(any(LoginDTO.class));
        verify(authResponsiveService, times(1)).handleSuccessfulAuthentication(any(HttpServletResponse.class), eq(mockUser), any());
    }

    @Test
    void testForgotPassword() throws Exception {
        String email = "test@example.com";
        doNothing().when(authService).forgotPassword(email);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .param("email", email))
                .andExpect(status().isOk());

        verify(authService, times(1)).forgotPassword(email);
    }

    @Test
    void testResetPassword() throws Exception {
        ResetPasswordDTO resetDto = new ResetPasswordDTO();
        doNothing().when(authService).resetPassword(any(ResetPasswordDTO.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetDto)))
                .andExpect(status().isOk());

        verify(authService, times(1)).resetPassword(any(ResetPasswordDTO.class));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"))
                .andExpect(cookie().value("JWT", (String) null))
                .andExpect(cookie().maxAge("JWT", 0))
                .andExpect(cookie().path("JWT", "/"));
    }
}