package com.payroll.payrollsystem.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollSummaryResponse {

    private String payrollMonth;
    private Long employeeCount;

    private BigDecimal totalGrossSalary;
    private BigDecimal totalNetSalary;
    private BigDecimal totalPayeTax;
    private BigDecimal totalRssb;
    private BigDecimal totalEmployerCost;
    private BigDecimal totalBonuses;

    private Long draftCount;
    private Long pendingCount;
    private Long approvedCount;
    private Long paidCount;
    private List<DepartmentSummary> departmentBreakdown;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DepartmentSummary {
        private String departmentName;
        private Long employeeCount;
        private BigDecimal totalGross;
        private BigDecimal totalNet;
    }
}
