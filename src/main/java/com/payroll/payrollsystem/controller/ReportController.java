package com.payroll.payrollsystem.controller;

import com.payroll.payrollsystem.dto.response.PayrollSummaryResponse;
import com.payroll.payrollsystem.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports",
        description = "Financial reports for Finance Manager, " +
                "Accountant and General Manager.")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Monthly payroll summary",
            description = "Total gross, net, tax, RSSB " +
                    "and department breakdown.")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER'," +
            "'ROLE_ACCOUNTANT')")
    @GetMapping("/monthly/{payrollMonth}")
    public ResponseEntity<PayrollSummaryResponse> monthly(
            @PathVariable String payrollMonth) {
        return ResponseEntity.ok(
                reportService.getMonthlySummary(payrollMonth));
    }
    @Operation(summary = "Yearly payroll summary",
            description = "Month by month breakdown " +
                    "for the entire year.")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER'," +
            "'ROLE_ACCOUNTANT')")
    @GetMapping("/yearly/{year}")
    public ResponseEntity<List<PayrollSummaryResponse>> yearly(
            @PathVariable String year) {
        return ResponseEntity.ok(
                reportService.getYearlySummary(year));
    }
}