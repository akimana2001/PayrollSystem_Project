package com.payroll.payrollsystem.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private boolean active;
    private Long employeeCount;
    private LocalDateTime createdAt;
}
