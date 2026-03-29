package com.payroll.payrollsystem.controller;

import com.payroll.payrollsystem.dto.response.PayrollRecordResponse;
import com.payroll.payrollsystem.dto.response.PayrollSummaryResponse;
import com.payroll.payrollsystem.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll Processing",
        description = "Run payroll, manage approval workflow " +
                "and view payslips.")
public class PayrollController {

    private final PayrollService payrollService;

    @Operation(summary = "Run monthly payroll",
            description = "Finance Manager only. " +
                    "Processes ALL active employees at once. " +
                    "Format: YYYY-MM e.g. 2024-03")
    @PreAuthorize("hasAuthority('ROLE_FINANCE_MANAGER')")
    @PostMapping("/run/{payrollMonth}")
    public ResponseEntity<List<PayrollRecordResponse>> run(
            @PathVariable String payrollMonth,
            Authentication auth) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(payrollService.runMonthlyPayroll(
                        payrollMonth, auth.getName()));
    }
    @Operation(summary = "Submit payroll for approval",
            description = "Finance Manager only. DRAFT → PENDING")
    @PreAuthorize("hasAuthority('ROLE_FINANCE_MANAGER')")
    @PostMapping("/submit/{payrollMonth}")
    public ResponseEntity<Void> submit(
            @PathVariable String payrollMonth,
            Authentication auth) {
        payrollService.submitForApproval(
                payrollMonth, auth.getName());
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Approve payroll",
            description = "Finance Manager or General Manager. " +
                    "PENDING → APPROVED")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER')")
    @PostMapping("/approve/{payrollMonth}")
    public ResponseEntity<Void> approve(
            @PathVariable String payrollMonth,
            Authentication auth) {
        payrollService.approvePayroll(
                payrollMonth, auth.getName());
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Mark payroll as paid",
            description = "Finance Manager or General Manager. " +
                    "APPROVED → PAID")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER')")
    @PostMapping("/pay/{payrollMonth}")
    public ResponseEntity<Void> markAsPaid(
            @PathVariable String payrollMonth,
            Authentication auth) {
        payrollService.markAsPaid(
                payrollMonth, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all payslips for a month")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER'," +
            "'ROLE_ACCOUNTANT')")
    @GetMapping("/{payrollMonth}")
    public ResponseEntity<List<PayrollRecordResponse>> getByMonth(
            @PathVariable String payrollMonth) {
        return ResponseEntity.ok(
                payrollService.getByMonth(payrollMonth));
    }
    @Operation(summary = "Get monthly financial summary")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER'," +
            "'ROLE_ACCOUNTANT')")
    @GetMapping("/summary/{payrollMonth}")
    public ResponseEntity<PayrollSummaryResponse> getSummary(
            @PathVariable String payrollMonth) {
        return ResponseEntity.ok(
                payrollService.getMonthlySummary(payrollMonth));
    }

    // Accountant needs payslip history for bookkeeping.
    @Operation(summary = "Get employee payslip history")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER'," +
            "'ROLE_ACCOUNTANT')")
    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<Page<PayrollRecordResponse>> getHistory(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(
                payrollService.getEmployeeHistory(
                        employeeId,
                        PageRequest.of(page, size,
                                Sort.by("payrollMonth")
                                        .descending())));
    }

    @Operation(summary = "Get all payroll months",
            description = "Used to populate month dropdowns in UI.")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER'," +
            "'ROLE_ACCOUNTANT')")
    @GetMapping("/months")
    public ResponseEntity<List<String>> getMonths() {
        return ResponseEntity.ok(
                payrollService.getAllPayrollMonths());
    }
}