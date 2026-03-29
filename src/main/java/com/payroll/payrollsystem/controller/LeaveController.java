package com.payroll.payrollsystem.controller;

import com.payroll.payrollsystem.dto.request.LeaveRequestDto;
import com.payroll.payrollsystem.dto.response.LeaveResponse;
import com.payroll.payrollsystem.service.LeaveService;
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
@RequestMapping("/v1/leave")
@RequiredArgsConstructor
@Tag(name = "Leave Management",
        description = "Submit and manage employee leave requests.")
public class LeaveController {

    private final LeaveService leaveService;

    @Operation(summary = "Submit leave request")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_HR_MANAGER'," +
            "'ROLE_GENERAL_MANAGER')")
    @PostMapping
    public ResponseEntity<LeaveResponse> submit(
            @Valid @RequestBody LeaveRequestDto request,
            Authentication auth) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leaveService.submit(
                        request, auth.getName()));
    }
    @Operation(summary = "Get all pending leave requests",
            description = "HR Manager only.")
    @PreAuthorize("hasAuthority('ROLE_HR_MANAGER')")
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveResponse>> getPending() {
        return ResponseEntity.ok(leaveService.getPending());
    }
    @Operation(summary = "Get leave requests by employee")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_HR_MANAGER'," +
            "'ROLE_GENERAL_MANAGER')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveResponse>> getByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                leaveService.getByEmployee(employeeId));
    }
    @Operation(summary = "Approve leave request",
            description = "HR Manager only.")
    @PreAuthorize("hasAuthority('ROLE_HR_MANAGER')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<LeaveResponse> approve(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            Authentication auth) {
        return ResponseEntity.ok(
                leaveService.approve(
                        id, comment, auth.getName()));
    }

    @Operation(summary = "Reject leave request",
            description = "HR Manager only.")
    @PreAuthorize("hasAuthority('ROLE_HR_MANAGER')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<LeaveResponse> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            Authentication auth) {
        return ResponseEntity.ok(
                leaveService.reject(
                        id, comment, auth.getName()));
    }
}