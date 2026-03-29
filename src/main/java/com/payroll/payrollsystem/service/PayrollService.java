package com.payroll.payrollsystem.service;

import com.payroll.payrollsystem.dto.response.PayrollRecordResponse;
import com.payroll.payrollsystem.dto.response.PayrollSummaryResponse;
import com.payroll.payrollsystem.exception.DuplicateResourceException;
import com.payroll.payrollsystem.exception.ResourceNotFoundException;
import com.payroll.payrollsystem.model.Employee;
import com.payroll.payrollsystem.model.PayrollRecord;
import com.payroll.payrollsystem.model.enums.PayrollStatus;
import com.payroll.payrollsystem.repository.EmployeeRepository;
import com.payroll.payrollsystem.repository.PayrollRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayrollService {

    private final PayrollRecordRepository payrollRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final TaxCalculationService taxCalculationService;
    private final AuditLogService auditLogService;

    @PreAuthorize("hasRole('ROLE_FINANCE_MANAGER')")
    public List<PayrollRecordResponse> runMonthlyPayroll(
            String payrollMonth, String processedBy) {

        log.info("Running payroll for month: {}", payrollMonth);

        List<Employee> activeEmployees =
                employeeRepository.findAllActiveWithDepartment();

        if (activeEmployees.isEmpty()) {
            throw new IllegalStateException(
                    "No active employees found");
        }

        List<PayrollRecord> records = new ArrayList<>();

        for (Employee employee : activeEmployees) {

            if (payrollRecordRepository
                    .existsByEmployeeIdAndPayrollMonth(
                            employee.getId(), payrollMonth)) {
                log.warn("Skipping {} — already processed",
                        employee.getEmployeeNumber());
                continue;
            }

            PayrollRecord record = buildPayrollRecord(
                    employee, payrollMonth,
                    BigDecimal.ZERO, processedBy);

            records.add(payrollRecordRepository.save(record));
        }

        auditLogService.log("PAYROLL_RUN", "PayrollRecord",
                null, "Payroll run for " + payrollMonth
                        + " — " + records.size() + " employees",
                processedBy);

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ROLE_FINANCE_MANAGER')")
    public void submitForApproval(String payrollMonth,
                                  String submittedBy) {

        List<PayrollRecord> drafts = payrollRecordRepository
                .findByPayrollMonthAndStatus(
                        payrollMonth, PayrollStatus.DRAFT);

        if (drafts.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No draft payroll found for: " + payrollMonth);
        }

        drafts.forEach(r -> r.setStatus(PayrollStatus.PENDING));
        payrollRecordRepository.saveAll(drafts);

        auditLogService.log("PAYROLL_SUBMITTED",
                "PayrollRecord", null,
                "Submitted payroll for approval: " + payrollMonth,
                submittedBy);
    }

    @PreAuthorize("hasAnyRole('ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER')")
    public void approvePayroll(String payrollMonth,
                               String approvedBy) {

        List<PayrollRecord> pending = payrollRecordRepository
                .findByPayrollMonthAndStatus(
                        payrollMonth, PayrollStatus.PENDING);

        if (pending.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No pending payroll found for: " + payrollMonth);
        }

        LocalDateTime now = LocalDateTime.now();
        pending.forEach(r -> {
            r.setStatus(PayrollStatus.APPROVED);
            r.setApprovedBy(approvedBy);
            r.setApprovedAt(now);
        });

        payrollRecordRepository.saveAll(pending);

        auditLogService.log("PAYROLL_APPROVED",
                "PayrollRecord", null,
                "Approved payroll: " + payrollMonth,
                approvedBy);
    }

    @PreAuthorize("hasAnyRole('ROLE_FINANCE_MANAGER'," +
            "'ROLE_GENERAL_MANAGER')")
    public void markAsPaid(String payrollMonth, String paidBy) {

        List<PayrollRecord> approved = payrollRecordRepository
                .findByPayrollMonthAndStatus(
                        payrollMonth, PayrollStatus.APPROVED);

        if (approved.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No approved payroll found for: "
                            + payrollMonth);
        }

        LocalDateTime now = LocalDateTime.now();
        approved.forEach(r -> {
            r.setStatus(PayrollStatus.PAID);
            r.setPaidAt(now);
        });

        payrollRecordRepository.saveAll(approved);

        auditLogService.log("PAYROLL_PAID",
                "PayrollRecord", null,
                "Marked as paid: " + payrollMonth, paidBy);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_FINANCE_MANAGER'," +
            "'ROLE_ACCOUNTANT'," +
            "'ROLE_GENERAL_MANAGER')")
    public List<PayrollRecordResponse> getByMonth(
            String payrollMonth) {
        return payrollRecordRepository
                .findByPayrollMonth(payrollMonth)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_HR_MANAGER'," +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_ACCOUNTANT'," +
            "'ROLE_GENERAL_MANAGER')")
    public Page<PayrollRecordResponse> getEmployeeHistory(
            Long employeeId, Pageable pageable) {
        return payrollRecordRepository
                .findByEmployeeIdOrderByPayrollMonthDesc(
                        employeeId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_FINANCE_MANAGER'," +
            "'ROLE_ACCOUNTANT'," +
            "'ROLE_GENERAL_MANAGER')")
    public PayrollSummaryResponse getMonthlySummary(
            String payrollMonth) {

        return PayrollSummaryResponse.builder()
                .payrollMonth(payrollMonth)
                .employeeCount(payrollRecordRepository
                        .countByMonth(payrollMonth))
                .totalGrossSalary(payrollRecordRepository
                        .sumGrossSalaryByMonth(payrollMonth))
                .totalNetSalary(payrollRecordRepository
                        .sumNetSalaryByMonth(payrollMonth))
                .totalPayeTax(payrollRecordRepository
                        .sumPayeTaxByMonth(payrollMonth))
                .totalRssb(payrollRecordRepository
                        .sumRssbByMonth(payrollMonth))
                .build();
    }

    @Transactional(readOnly = true)
    public List<String> getAllPayrollMonths() {
        return payrollRecordRepository.findAllPayrollMonths();
    }

    private PayrollRecord buildPayrollRecord(
            Employee employee,
            String payrollMonth,
            BigDecimal bonus,
            String createdBy) {

        BigDecimal basicSalary = employee.getBasicSalary();
        BigDecimal housing = employee.getHousingAllowance();
        BigDecimal transport = employee.getTransportAllowance();
        BigDecimal medical = employee.getMedicalAllowance();
        BigDecimal other = employee.getOtherAllowance();

        BigDecimal gross = basicSalary
                .add(housing).add(transport)
                .add(medical).add(other)
                .add(bonus);

        BigDecimal payeTax =
                taxCalculationService.calculatePAYE(gross);
        BigDecimal rssbEmployee =
                taxCalculationService.calculateRssbEmployee(gross);
        BigDecimal rssbEmployer =
                taxCalculationService.calculateRssbEmployer(gross);

        BigDecimal totalDeductions = payeTax.add(rssbEmployee);
        BigDecimal netSalary = gross.subtract(totalDeductions);

        return PayrollRecord.builder()
                .employee(employee)
                .payrollMonth(payrollMonth)
                .payDate(LocalDate.now())
                .basicSalary(basicSalary)
                .housingAllowance(housing)
                .transportAllowance(transport)
                .medicalAllowance(medical)
                .otherAllowance(other)
                .bonus(bonus)
                .grossSalary(gross)
                .payeTax(payeTax)
                .rssbEmployee(rssbEmployee)
                .rssbEmployer(rssbEmployer)
                .totalDeductions(totalDeductions)
                .netSalary(netSalary)
                .status(PayrollStatus.DRAFT)
                .createdBy(createdBy)
                .build();
    }
    private PayrollRecordResponse mapToResponse(PayrollRecord r) {
        return PayrollRecordResponse.builder()
                .id(r.getId())
                .payrollMonth(r.getPayrollMonth())
                .payDate(r.getPayDate())
                .employeeId(r.getEmployee().getId())
                .employeeNumber(r.getEmployee().getEmployeeNumber())
                .employeeName(r.getEmployee().getFullName())
                .departmentName(r.getEmployee()
                        .getDepartment().getName())
                .basicSalary(r.getBasicSalary())
                .housingAllowance(r.getHousingAllowance())
                .transportAllowance(r.getTransportAllowance())
                .medicalAllowance(r.getMedicalAllowance())
                .otherAllowance(r.getOtherAllowance())
                .bonus(r.getBonus())
                .grossSalary(r.getGrossSalary())
                .payeTax(r.getPayeTax())
                .rssbEmployee(r.getRssbEmployee())
                .rssbEmployer(r.getRssbEmployer())
                .totalDeductions(r.getTotalDeductions())
                .netSalary(r.getNetSalary())
                .status(r.getStatus())
                .approvedBy(r.getApprovedBy())
                .approvedAt(r.getApprovedAt())
                .paidAt(r.getPaidAt())
                .createdAt(r.getCreatedAt())
                .createdBy(r.getCreatedBy())
                .build();
    }
}
