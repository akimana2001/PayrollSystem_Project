package com.payroll.payrollsystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    @NotBlank(message = "Payroll month is required")
    private String payrollMonth;

    @DecimalMin(value = "0.0",
            message = "Bonus cannot be negative")
    private BigDecimal bonus;

}
