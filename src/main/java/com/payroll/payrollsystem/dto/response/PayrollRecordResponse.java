package com.payroll.payrollsystem.dto.response;

import com.payroll.payrollsystem.model.enums.PayrollStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRecordResponse {

    private Long id;
    private String payrollMonth;
    private LocalDate payDate;

    private Long employeeId;
    private String employeeNumber;
    private String employeeName;
    private String departmentName;

    private BigDecimal basicSalary;
    private BigDecimal housingAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal medicalAllowance;
    private BigDecimal otherAllowance;
    private BigDecimal bonus;
    private BigDecimal grossSalary;

    private BigDecimal payeTax;
    private BigDecimal rssbEmployee;
    private BigDecimal rssbEmployer;
    private BigDecimal medicalDeduction;
    private BigDecimal loanDeduction;
    private BigDecimal otherDeductions;
    private BigDecimal totalDeductions;

    private BigDecimal netSalary;
    private BigDecimal totalEmployerCost;

    private Integer workingDays;
    private Integer daysWorked;

    private PayrollStatus status;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
    private String createdBy;
}
