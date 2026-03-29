package com.payroll.payrollsystem.controller;

import com.payroll.payrollsystem.model.User;
import com.payroll.payrollsystem.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management",
        description = "Manage system users. " +
                "General Manager has full access.")
public class UserController {

    private final UserRepository userRepository;

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(
                userRepository.findAll());
    }
    @Operation(summary = "Delete user",
            description = "General Manager only.")
    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}