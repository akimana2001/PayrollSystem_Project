package com.payroll.payrollsystem.controller;

import com.payroll.payrollsystem.dto.request.AuthRequest;
import com.payroll.payrollsystem.dto.response.AuthResponse;
import com.payroll.payrollsystem.model.enums.Role;
import com.payroll.payrollsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication",
        description = "Login and register — no token required")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Login",
            description = "Returns JWT token. " +
                    "Use token in all other endpoints.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(
                authService.login(request));
    }
    @Operation(summary = "Register new user",
            description = "Creates new user account with role.")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam Role role) {
        return ResponseEntity.ok(
                authService.register(
                        username, password, email, role));
    }
}
