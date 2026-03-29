package com.payroll.payrollsystem.controller;

import com.payroll.payrollsystem.security.JwtService;
import com.payroll.payrollsystem.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.payroll.payrollsystem.config.SecurityConfig;
import com.payroll.payrollsystem.config.ApplicationConfig;
import com.payroll.payrollsystem.repository.UserRepository;
import com.payroll.payrollsystem.security.JwtAuthenticationFilter;
import com.payroll.payrollsystem.security.OAuth2UserService;
import com.payroll.payrollsystem.security.OAuth2SuccessHandler;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class,
        ApplicationConfig.class,
        JwtAuthenticationFilter.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private OAuth2UserService oAuth2UserService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Test
    void getAllUsers_withoutAuthentication_returns200()
            throws Exception {

        when(userRepository.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_withUserRole_returns403()
            throws Exception {

        mockMvc.perform(delete("/v1/users/1"))
                .andExpect(status().isForbidden());
    }

    @org.testng.annotations.Test
    @WithMockUser(roles = "GENERAL_MANAGER")
    void deleteUser_withGeneralManagerRole_returns204()
            throws Exception {


        mockMvc.perform(delete("/v1/users/1"))
                .andExpect(status().isNoContent());
    }
}
