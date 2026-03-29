package com.payroll.payrollsystem.controller;

import com.payroll.payrollsystem.dto.request.DepartmentRequest;
import com.payroll.payrollsystem.dto.response.DepartmentResponse;
import com.payroll.payrollsystem.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management",
        description = "Manage company departments. HR Manager only.")
public class DepartmentController {

    private final DepartmentService departmentService;
    @Operation(summary = "Create department")
    @PreAuthorize("hasAuthority('ROLE_HR_MANAGER')")
    @PostMapping
    public ResponseEntity<DepartmentResponse> create(
            @Valid @RequestBody DepartmentRequest request,
            Authentication auth) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(departmentService.create(
                        request, auth.getName()));
    }
    @Operation(summary = "Get all active departments")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_HR_MANAGER'," +
            "'ROLE_GENERAL_MANAGER'," +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_ACCOUNTANT')")
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(departmentService.getAll());
    }
    @Operation(summary = "Get department by ID")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_HR_MANAGER'," +
            "'ROLE_GENERAL_MANAGER'," +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_ACCOUNTANT')")
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @Operation(summary = "Update department")
    @PreAuthorize("hasAuthority('ROLE_HR_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                departmentService.update(
                        id, request, auth.getName()));
    }
    @Operation(summary = "Deactivate department",
            description = "Soft delete — sets active to false.")
    @PreAuthorize("hasAuthority('ROLE_HR_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id,
            Authentication auth) {
        departmentService.deactivate(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}