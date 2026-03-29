package com.payroll.payrollsystem.service;

import com.payroll.payrollsystem.dto.response.PayrollSummaryResponse;
import com.payroll.payrollsystem.repository.PayrollRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final PayrollRecordRepository payrollRecordRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_FINANCE_MANAGER'," +
            "'ROLE_ACCOUNTANT'," +
            "'ROLE_GENERAL_MANAGER')")
    public PayrollSummaryResponse getMonthlySummary(
            String payrollMonth) {

        BigDecimal gross = payrollRecordRepository
                .sumGrossSalaryByMonth(payrollMonth);
        BigDecimal net = payrollRecordRepository
                .sumNetSalaryByMonth(payrollMonth);
        BigDecimal tax = payrollRecordRepository
                .sumPayeTaxByMonth(payrollMonth);
        BigDecimal rssb = payrollRecordRepository
                .sumRssbByMonth(payrollMonth);
        Long count = payrollRecordRepository
                .countByMonth(payrollMonth);

        List<Object[]> deptData = payrollRecordRepository
                .payrollCostByDepartment(payrollMonth);

        List<PayrollSummaryResponse.DepartmentSummary> breakdown =
                deptData.stream().map(row ->
                        PayrollSummaryResponse.DepartmentSummary
                                .builder()
                                .departmentName((String) row[0])
                                .totalGross((BigDecimal) row[1])
                                .totalNet((BigDecimal) row[2])
                                .employeeCount((Long) row[3])
                                .build()
                ).collect(Collectors.toList());

        return PayrollSummaryResponse.builder()
                .payrollMonth(payrollMonth)
                .employeeCount(count)
                .totalGrossSalary(gross)
                .totalNetSalary(net)
                .totalPayeTax(tax)
                .totalRssb(rssb)
                .totalEmployerCost(gross.add(rssb))
                .departmentBreakdown(breakdown)
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER')")
    public List<PayrollSummaryResponse> getYearlySummary(
            String year) {

        return payrollRecordRepository
                .yearlyPayrollSummary(year)
                .stream()
                .map(row -> PayrollSummaryResponse.builder()
                        .payrollMonth((String) row[0])
                        .totalGrossSalary((BigDecimal) row[1])
                        .totalNetSalary((BigDecimal) row[2])
                        .totalPayeTax((BigDecimal) row[3])
                        .employeeCount((Long) row[4])
                        .build())
                .collect(Collectors.toList());
    }
}
