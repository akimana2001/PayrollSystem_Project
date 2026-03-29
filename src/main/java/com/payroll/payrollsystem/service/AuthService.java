package com.payroll.payrollsystem.service;

import com.payroll.payrollsystem.dto.request.AuthRequest;
import com.payroll.payrollsystem.dto.response.AuthResponse;
import com.payroll.payrollsystem.exception.DuplicateResourceException;
import com.payroll.payrollsystem.model.User;
import com.payroll.payrollsystem.model.enums.Role;
import com.payroll.payrollsystem.repository.UserRepository;
import com.payroll.payrollsystem.security.CustomUserDetailsService;
import com.payroll.payrollsystem.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse login(AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        UserDetails userDetails = userDetailsService
                .loadUserByUsername(request.getUsername());

        String token = jwtService.generateToken(userDetails);

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow();

        log.info("User logged in: {}", request.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
    public AuthResponse register(String username,
                                 String password,
                                 String email,
                                 Role role) {

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException(
                    "Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "Email already registered: " + email);
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .role(role)
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("New user registered: {} role: {}",
                username, role);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}