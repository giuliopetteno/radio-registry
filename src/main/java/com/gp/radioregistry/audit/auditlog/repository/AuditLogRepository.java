package com.gp.radioregistry.audit.auditlog.repository;

import com.gp.radioregistry.audit.auditlog.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}

