package com.payroll.payrollsystem.controller;

import com.payroll.payrollsystem.dto.request.EmployeeRequest;
import com.payroll.payrollsystem.dto.request.TerminationRequest;
import com.payroll.payrollsystem.dto.response.EmployeeResponse;
import com.payroll.payrollsystem.model.enums.EmploymentStatus;
import com.payroll.payrollsystem.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management",
        description = "Create, read, update and terminate employees. " +
                "HR Manager has full access.")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "Create employee",
            description = "HR Manager only. " +
                    "Employee number is auto-generated.")
    @PreAuthorize("hasAuthority('ROLE_HR_MANAGER')")
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(
            @Valid @RequestBody EmployeeRequest request,
            Authentication auth) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employeeService.create(
                        request, auth.getName()));
    }
    @Operation(summary = "Search employees",
            description = "Filter by name, department or status. " +
                    "Results are paginated.")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_HR_MANAGER'," +
            "'ROLE_GENERAL_MANAGER'," +
            "'ROLE_FINANCE_MANAGER')")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                employeeService.search(
                        search, departmentId, status,
                        PageRequest.of(page, size,
                                Sort.by("lastName").ascending())));
    }
    @Operation(summary = "Get employee by ID")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_HR_MANAGER'," +
            "'ROLE_GENERAL_MANAGER'," +
            "'ROLE_FINANCE_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @Operation(summary = "Update employee",
            description = "HR Manager only.")
    @PreAuthorize("hasAuthority('ROLE_HR_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                employeeService.update(
                        id, request, auth.getName()));
    }
    @Operation(summary = "Terminate employee",
            description = "HR Manager only. " +
                    "Soft delete — record is never removed.")
    @PreAuthorize("hasAuthority('ROLE_HR_MANAGER')")
    @PatchMapping("/{id}/terminate")
    public ResponseEntity<Void> terminate(
            @PathVariable Long id,
            @Valid @RequestBody TerminationRequest request,
            Authentication auth) {
        employeeService.terminate(id, request, auth.getName());
        return ResponseEntity.noContent().build();
    }
}