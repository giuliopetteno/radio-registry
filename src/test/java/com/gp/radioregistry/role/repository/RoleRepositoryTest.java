package com.gp.radioregistry.role.repository;

import com.gp.radioregistry.base.AbstractPostgresContainerTest;
import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
class RoleRepositoryTest extends AbstractPostgresContainerTest {

    private static final String ROLE_NAME = "USER";
    private static final String ROLE_NAME_SECONDARY = "OPERATOR";
    private static final String ROLE_NAME_UNKNOWN = "UNKNOWN";

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName("should persist role and generate id")
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
    @DisplayName("should return persisted role by id")
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
    @DisplayName("should return empty when role does not exist")
    void findByIdReturnsEmptyWhenRoleDoesNotExist() {
        Optional<Role> found = roleRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should return all persisted roles")
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
    @DisplayName("should return the number of roles")
    void countReturnsNumberOfRoles() {
        roleRepository.save(Role.builder().name(ROLE_NAME).build());

        assertThat(roleRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should remove the role")
    void deleteRemovesRole() {
        var saved = roleRepository.save(Role.builder().name(ROLE_NAME).build());
        entityManager.flush();

        roleRepository.delete(saved);
        entityManager.flush();

        assertThat(roleRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("should return matching role by name")
    void findByNameReturnsMatchingRole() {
        roleRepository.save(Role.builder().name(ROLE_NAME).build());
        entityManager.flush();
        entityManager.clear();

        Optional<Role> found = roleRepository.findByName(ROLE_NAME);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(ROLE_NAME);
    }

    @Test
    @DisplayName("should return empty when no role matches the name")
    void findByNameReturnsEmptyWhenNoRoleMatches() {
        Optional<Role> found = roleRepository.findByName(ROLE_NAME_UNKNOWN);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should return true when role exists by name")
    void existsByNameReturnsTrueWhenRoleExists() {
        roleRepository.save(Role.builder().name(ROLE_NAME).build());
        entityManager.flush();

        assertThat(roleRepository.existsByName(ROLE_NAME)).isTrue();
    }

    @Test
    @DisplayName("should return false when role does not exist by name")
    void existsByNameReturnsFalseWhenRoleDoesNotExist() {
        assertThat(roleRepository.existsByName(ROLE_NAME_UNKNOWN)).isFalse();
    }

    @Test
    @DisplayName("should violate not-null constraint when saving role without name")
    void savingRoleWithoutNameViolatesNotNullConstraint() {
        var role = Role.builder().build();

        assertThatThrownBy(() -> {
            roleRepository.save(role);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("should violate unique constraint when saving role with duplicate name")
    void savingRoleWithDuplicateNameViolatesUniqueConstraint() {
        roleRepository.save(Role.builder().name(ROLE_NAME).build());
        entityManager.flush();

        assertThatThrownBy(() -> {
            roleRepository.save(Role.builder().name(ROLE_NAME).build());
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
