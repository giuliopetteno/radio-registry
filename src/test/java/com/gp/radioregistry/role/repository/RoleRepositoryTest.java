package com.gp.radioregistry.role.repository;

import com.gp.radioregistry.config.AbstractPostgresContainerTest;
import com.gp.radioregistry.role.domain.Role;
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
class RoleRepositoryTest extends AbstractPostgresContainerTest {

    private static final String ROLE_NAME = "ADMIN";
    private static final String ROLE_NAME_SECONDARY = "OPERATOR";
    private static final String ROLE_NAME_UNKNOWN = "UNKNOWN";

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savePersistsRoleAndGeneratesId() {
        var role = Role.builder()
                .name(ROLE_NAME)
                .build();

        var saved = roleRepository.save(role);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByIdReturnsPersistedRole() {
        var saved = roleRepository.save(Role.builder()
                .name(ROLE_NAME)
                .build());
        entityManager.flush();
        entityManager.clear();

        Optional<Role> found = roleRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(ROLE_NAME);
    }

    @Test
    void findByIdReturnsEmptyWhenRoleDoesNotExist() {
        Optional<Role> found = roleRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAllReturnsAllPersistedRoles() {
        roleRepository.save(Role.builder().name(ROLE_NAME).build());
        roleRepository.save(Role.builder().name(ROLE_NAME_SECONDARY).build());

        List<Role> roles = roleRepository.findAll();

        assertThat(roles)
                .hasSize(2)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(ROLE_NAME, ROLE_NAME_SECONDARY);
    }

    @Test
    void countReturnsNumberOfRoles() {
        roleRepository.save(Role.builder().name(ROLE_NAME).build());

        assertThat(roleRepository.count()).isEqualTo(1);
    }

    @Test
    void deleteRemovesRole() {
        var saved = roleRepository.save(Role.builder().name(ROLE_NAME).build());
        entityManager.flush();

        roleRepository.delete(saved);
        entityManager.flush();

        assertThat(roleRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void findByNameReturnsMatchingRole() {
        roleRepository.save(Role.builder().name(ROLE_NAME).build());
        entityManager.flush();
        entityManager.clear();

        Optional<Role> found = roleRepository.findByName(ROLE_NAME);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(ROLE_NAME);
    }

    @Test
    void findByNameReturnsEmptyWhenNoRoleMatches() {
        Optional<Role> found = roleRepository.findByName(ROLE_NAME_UNKNOWN);

        assertThat(found).isEmpty();
    }

    @Test
    void existsByNameReturnsTrueWhenRoleExists() {
        roleRepository.save(Role.builder().name(ROLE_NAME).build());
        entityManager.flush();

        assertThat(roleRepository.existsByName(ROLE_NAME)).isTrue();
    }

    @Test
    void existsByNameReturnsFalseWhenRoleDoesNotExist() {
        assertThat(roleRepository.existsByName(ROLE_NAME_UNKNOWN)).isFalse();
    }

    @Test
    void savingRoleWithoutNameViolatesNotNullConstraint() {
        var role = Role.builder().build();

        assertThatThrownBy(() -> {
            roleRepository.save(role);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void savingRoleWithDuplicateNameViolatesUniqueConstraint() {
        roleRepository.save(Role.builder().name(ROLE_NAME).build());
        entityManager.flush();

        assertThatThrownBy(() -> {
            roleRepository.save(Role.builder().name(ROLE_NAME).build());
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}

