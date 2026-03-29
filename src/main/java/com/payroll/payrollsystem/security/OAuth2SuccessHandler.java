package com.payroll.payrollsystem.security;

import com.payroll.payrollsystem.model.User;
import com.payroll.payrollsystem.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // Method name MUST be exactly "onAuthenticationSuccess"
    // Spring Security looks for this exact method name
    // to call after a successful OAuth2 login.
    // If the name is wrong Spring skips it entirely.
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User =
                (OAuth2User) authentication.getPrincipal();

        // Get email — works for both Google and GitHub
        String email = oAuth2User.getAttribute("email");

        // GitHub private email fallback
        if (email == null) {
            String login =
                    oAuth2User.getAttribute("login");
            email = login + "@github-user.com";
        }

        Optional<User> userOptional =
                userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "User not found");
            return;
        }

        User user = userOptional.get();

        // Build UserDetails to generate JWT token
        var userDetails =
                new org.springframework.security
                        .core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        List.of(new SimpleGrantedAuthority(
                                user.getRole().name()))
                );

        String token = jwtService.generateToken(userDetails);

        log.info("JWT issued for OAuth2 user: {}", email);

        // Return token in response body as JSON
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(
                "{\"token\":\"" + token + "\"," +
                        "\"tokenType\":\"Bearer\"," +
                        "\"username\":\"" +
                        user.getUsername() + "\"," +
                        "\"role\":\"" +
                        user.getRole() + "\"}");
    }
}
