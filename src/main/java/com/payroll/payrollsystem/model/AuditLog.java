package com.payroll.payrollsystem.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue
              (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
            (nullable = false)
    private String action;

    @Column
            (nullable = false)
    private String entityType;

    private Long entityId;

    @Column
            (length = 1000)
    private String description;

    @Column
            (nullable = false)
    private String performedBy;

    private String ipAddress;

    @CreationTimestamp
    @Column
            (nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
