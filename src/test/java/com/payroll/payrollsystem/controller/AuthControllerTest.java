package com.payroll.payrollsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payroll.payrollsystem.config.ApplicationConfig;
import com.payroll.payrollsystem.config.SecurityConfig;
import com.payroll.payrollsystem.dto.request.AuthRequest;
import com.payroll.payrollsystem.dto.response.AuthResponse;
import com.payroll.payrollsystem.model.enums.Role;
import com.payroll.payrollsystem.security.CustomUserDetailsService;
import com.payroll.payrollsystem.security.JwtAuthenticationFilter;
import com.payroll.payrollsystem.security.JwtService;
import com.payroll.payrollsystem.security.OAuth2SuccessHandler;
import com.payroll.payrollsystem.security.OAuth2UserService;
import com.payroll.payrollsystem.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class,
        ApplicationConfig.class,
        JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private OAuth2UserService oAuth2UserService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Test
    void login_withValidCredentials_returnsJwtToken()
            throws Exception {

        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        AuthResponse fakeResponse = AuthResponse.builder()
                .token("fake-jwt-token-for-testing")
                .tokenType("Bearer")
                .username("testuser")
                .role(Role.ROLE_HR_MANAGER)
                .build();

        when(authService.login(any(AuthRequest.class)))
                .thenReturn(fakeResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(request)))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.token")
                        .value("fake-jwt-token-for-testing"))

                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))

                .andExpect(jsonPath("$.username")
                        .value("testuser"));
    }

    @Test
    void login_withEmptyUsername_returns400()
            throws Exception {

        AuthRequest badRequest = new AuthRequest();
        badRequest.setUsername("");
        badRequest.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }
}
