package com.payroll.payrollsystem.dto.response;

import com.payroll.payrollsystem.model.enums.EmploymentStatus;
import com.payroll.payrollsystem.model.enums.SalaryType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private Long id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String jobTitle;
    private Long departmentId;
    private String departmentName;
    private LocalDate dateOfJoining;
    private LocalDate dateOfTermination;
    private EmploymentStatus status;

    // compensation
    private BigDecimal basicSalary;
    private BigDecimal housingAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal medicalAllowance;
    private BigDecimal otherAllowance;
    private BigDecimal grossSalary;
    private SalaryType salaryType;

    // masked — shows only last 4 digits
    private String bankName;
    private String bankAccountNumber;

    private LocalDateTime createdAt;
    private String createdBy;
}
