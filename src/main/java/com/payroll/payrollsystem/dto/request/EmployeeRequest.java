package com.payroll.payrollsystem.dto.request;

import com.payroll.payrollsystem.model.enums.SalaryType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "National ID is required")
    private String nationalId;

    private LocalDate dateOfBirth;
    private String gender;
    private String address;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Date of joining is required")
    private LocalDate dateOfJoining;

    @NotNull(message = "Basic salary is required")
    @DecimalMin(value = "0.01",
            message = "Salary must be greater than zero")
    private BigDecimal basicSalary;

    @DecimalMin(value = "0.0",
            message = "Allowance cannot be negative")
    private BigDecimal housingAllowance;

    @DecimalMin(value = "0.0",
            message = "Allowance cannot be negative")
    private BigDecimal transportAllowance;

    @DecimalMin(value = "0.0",
            message = "Allowance cannot be negative")
    private BigDecimal medicalAllowance;

    @DecimalMin(value = "0.0",
            message = "Allowance cannot be negative")
    private BigDecimal otherAllowance;

    private SalaryType salaryType;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    private String bankBranch;
}