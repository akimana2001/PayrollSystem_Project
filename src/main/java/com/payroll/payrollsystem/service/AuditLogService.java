package com.payroll.payrollsystem.service;

import com.payroll.payrollsystem.model.AuditLog;
import com.payroll.payrollsystem.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action,
                    String entityType,
                    Long entityId,
                    String description,
                    String performedBy) {

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .performedBy(performedBy)
                .build();

        auditLogRepository.save(auditLog);
        log.info("AUDIT | {} | {} | {} | {}",
                action, entityType, entityId, performedBy);
    }

    public Page<AuditLog> getLogsByUser(String username,
                                        Pageable pageable) {
        return auditLogRepository
                .findByPerformedByOrderByCreatedAtDesc(
                        username, pageable);
    }
    public Page<AuditLog> getLogsByEntity(String entityType,
                                          Long entityId,
                                          Pageable pageable) {
        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        entityType, entityId, pageable);
    }
}
