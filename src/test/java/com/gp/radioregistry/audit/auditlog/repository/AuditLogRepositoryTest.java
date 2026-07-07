package com.gp.radioregistry.audit.auditlog.repository;

import com.gp.radioregistry.audit.auditlog.domain.AuditLog;
import com.gp.radioregistry.base.AbstractPostgresContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class AuditLogRepositoryTest extends AbstractPostgresContainerTest {

    private static final String AUDIT_USERNAME = "username";
    private static final String AUDIT_ACTION = "CREATE";
    private static final String AUDIT_ACTION_SECONDARY = "DELETE";
    private static final String AUDIT_ENTITY_TYPE = "Department";
    private static final String AUDIT_ENTITY_ID = "1";
    private static final String AUDIT_DESCRIPTION = "Created a department";
    private static final String AUDIT_IP_ADDRESS = "192.168.0.1";

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("should persist audit log and generate id")
    void savePersistsAuditLogAndGeneratesId() {
        var auditLog = AuditLog.builder()
                .username(AUDIT_USERNAME)
                .action(AUDIT_ACTION)
                .entityType(AUDIT_ENTITY_TYPE)
                .entityId(AUDIT_ENTITY_ID)
                .description(AUDIT_DESCRIPTION)
                .ipAddress(AUDIT_IP_ADDRESS)
                .success(true)
                .build();

        var saved = auditLogRepository.save(auditLog);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should return persisted audit log by id")
    void findByIdReturnsPersistedAuditLog() {
        var saved = auditLogRepository.save(AuditLog.builder()
                .username(AUDIT_USERNAME)
                .action(AUDIT_ACTION)
                .entityType(AUDIT_ENTITY_TYPE)
                .entityId(AUDIT_ENTITY_ID)
                .description(AUDIT_DESCRIPTION)
                .success(true)
                .build());
        entityManager.flush();
        entityManager.clear();

        Optional<AuditLog> found = auditLogRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(AUDIT_USERNAME);
        assertThat(found.get().getAction()).isEqualTo(AUDIT_ACTION);
        assertThat(found.get().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("should return empty when audit log does not exist")
    void findByIdReturnsEmptyWhenAuditLogDoesNotExist() {
        Optional<AuditLog> found = auditLogRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should return all persisted audit logs")
    void findAllReturnsAllPersistedAuditLogs() {
        auditLogRepository.save(AuditLog.builder()
                .username(AUDIT_USERNAME)
                .action(AUDIT_ACTION)
                .success(true)
                .build());
        auditLogRepository.save(AuditLog.builder()
                .username(AUDIT_USERNAME)
                .action(AUDIT_ACTION_SECONDARY)
                .success(false)
                .build());

        List<AuditLog> auditLogs = auditLogRepository.findAll();

        assertThat(auditLogs)
                .hasSize(2)
                .extracting(AuditLog::getAction)
                .containsExactlyInAnyOrder(AUDIT_ACTION, AUDIT_ACTION_SECONDARY);
    }

    @Test
    @DisplayName("should return the number of audit logs")
    void countReturnsNumberOfAuditLogs() {
        auditLogRepository.save(AuditLog.builder()
                .action(AUDIT_ACTION)
                .success(true)
                .build());

        assertThat(auditLogRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should remove the audit log")
    void deleteRemovesAuditLog() {
        var saved = auditLogRepository.save(AuditLog.builder()
                .action(AUDIT_ACTION)
                .success(true)
                .build());
        entityManager.flush();

        auditLogRepository.delete(saved);
        entityManager.flush();

        assertThat(auditLogRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("should violate not-null constraint when saving audit log without action")
    void savingAuditLogWithoutActionViolatesNotNullConstraint() {
        var auditLog = AuditLog.builder()
                .username(AUDIT_USERNAME)
                .success(true)
                .build();

        assertThatThrownBy(() -> {
            auditLogRepository.save(auditLog);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
