package com.fintech.backend.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(auditable)")
    public Object auditMethodCall(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String action = auditable.action();
        String entityType = auditable.entityType();
        String performedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            logAudit(action, entityType, null, performedBy, "SUCCESS", null);
        } catch (Exception e) {
            exception = e;
            logAudit(action, entityType, null, performedBy, "FAILURE", e.getMessage());
            throw e;
        }

        return result;
    }

    private void logAudit(String action, String entityType, Long entityId, String performedBy,
            String status, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .performedBy(performedBy)
                    .timestamp(LocalDateTime.now())
                    .status(status)
                    .details(details)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }
}
