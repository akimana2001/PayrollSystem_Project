package com.payroll.payrollsystem.controller;

import com.payroll.payrollsystem.model.User;
import com.payroll.payrollsystem.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/me")
@RequiredArgsConstructor
@Tag(name = "My Profile",
        description = "Returns the currently logged in " +
                "user's profile. Works for both JWT " +
                "and OAuth2 logins.")
public class MeController {

    private final UserRepository userRepository;

    @Operation(summary = "Get my profile",
            description = "Returns username, email and role " +
                    "of the currently authenticated user.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> me(
            Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + username));

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail() != null
                        ? user.getEmail() : "",
                "role", user.getRole().name(),
                "enabled", user.isEnabled()
        ));
    }
}
