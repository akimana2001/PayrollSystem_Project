package com.payroll.payrollsystem.dto.response;

import com.payroll.payrollsystem.model.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    private String tokenType = "Bearer";
    private String username;
    private Role role;
}
