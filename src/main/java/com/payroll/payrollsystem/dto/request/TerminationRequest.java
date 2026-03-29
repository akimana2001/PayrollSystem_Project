package com.payroll.payrollsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TerminationRequest {

    @NotNull(message = "Termination date is required")
    private LocalDate terminationDate;
    private String reason;
}
