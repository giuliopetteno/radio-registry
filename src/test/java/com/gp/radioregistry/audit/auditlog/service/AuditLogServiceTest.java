package com.gp.radioregistry.audit.auditlog.service;

import com.gp.radioregistry.audit.auditlog.domain.AuditLog;
import com.gp.radioregistry.audit.auditlog.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService unit tests")
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = AuditLog.builder()
                .username("technician1")
                .action("CREATE")
                .entityType("USER")
                .success(true)
                .build();
    }

    @Test
    @DisplayName("should save the audit log through the repository")
    void saveAuditLog_persists() {
        auditLogService.saveAuditLog(auditLog);

        verify(auditLogRepository).save(auditLog);
    }

    @Test
    @DisplayName("should propagate exceptions thrown by the repository")
    void saveAuditLog_swallowsException() {
        doThrow(new RuntimeException("error from repository")).when(auditLogRepository).save(auditLog);

        assertDoesNotThrow(() -> auditLogService.saveAuditLog(auditLog));
        verify(auditLogRepository).save(auditLog);
    }
}

