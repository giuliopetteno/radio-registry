package com.gp.radioregistry.user.repository;

import com.gp.radioregistry.config.AbstractPostgresContainerTest;
import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest extends AbstractPostgresContainerTest {

    private static final String USERNAME = "username";
    private static final String EMAIL = "username@example.com";
    private static final String PASSWORD = "password";
    private static final String USERNAME_SECONDARY = "user2";
    private static final String EMAIL_SECONDARY = "user2@example.com";
    private static final String UNKNOWN_VALUE = "unknown";
    private static final String ROLE_NAME = "ADMIN";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User.UserBuilder baseUser() {
        return User.builder()
                .username(USERNAME)
                .email(EMAIL)
                .password(PASSWORD);
    }

    @Test
    void savePersistsUserAndGeneratesId() {
        var saved = userRepository.save(baseUser().build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.isAccountNonLocked()).isTrue();
    }

    @Test
    void findByIdReturnsPersistedUser() {
        var saved = userRepository.save(baseUser().build());
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(USERNAME);
        assertThat(found.get().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void findByIdReturnsEmptyWhenUserDoesNotExist() {
        Optional<User> found = userRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAllReturnsAllPersistedUsers() {
        userRepository.save(baseUser().build());
        userRepository.save(User.builder()
                .username(USERNAME_SECONDARY)
                .email(EMAIL_SECONDARY)
                .password(PASSWORD)
                .build());

        List<User> users = userRepository.findAll();

        assertThat(users)
                .hasSize(2)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder(USERNAME, USERNAME_SECONDARY);
    }

    @Test
    void countReturnsNumberOfUsers() {
        userRepository.save(baseUser().build());

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void deleteRemovesUser() {
        var saved = userRepository.save(baseUser().build());
        entityManager.flush();

        userRepository.delete(saved);
        entityManager.flush();

        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void persistsRoleAssociations() {
        var role = entityManager.persistFlushFind(Role.builder().name(ROLE_NAME).build());

        var saved = userRepository.save(baseUser().roles(Set.of(role)).build());
        entityManager.flush();
        entityManager.clear();

        var found = userRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getRoles())
                .hasSize(1)
                .extracting(Role::getName)
                .containsExactly(ROLE_NAME);
    }

    @Test
    void findByUsernameReturnsMatchingUser() {
        userRepository.save(baseUser().build());
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByUsername(USERNAME);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void findByUsernameReturnsEmptyWhenNoUserMatches() {
        assertThat(userRepository.findByUsername(UNKNOWN_VALUE)).isEmpty();
    }

    @Test
    void findByEmailReturnsMatchingUser() {
        userRepository.save(baseUser().build());
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByEmail(EMAIL);

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(USERNAME);
    }

    @Test
    void findByEmailReturnsEmptyWhenNoUserMatches() {
        assertThat(userRepository.findByEmail(UNKNOWN_VALUE)).isEmpty();
    }

    @Test
    void findByUsernameOrEmailMatchesByUsername() {
        userRepository.save(baseUser().build());
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByUsernameOrEmail(USERNAME, UNKNOWN_VALUE);

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(USERNAME);
    }

    @Test
    void findByUsernameOrEmailMatchesByEmail() {
        userRepository.save(baseUser().build());
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByUsernameOrEmail(UNKNOWN_VALUE, EMAIL);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void findByUsernameOrEmailReturnsEmptyWhenNoUserMatches() {
        assertThat(userRepository.findByUsernameOrEmail(UNKNOWN_VALUE, UNKNOWN_VALUE)).isEmpty();
    }

    @Test
    void existsByUsernameReturnsTrueWhenUserExists() {
        userRepository.save(baseUser().build());
        entityManager.flush();

        assertThat(userRepository.existsByUsername(USERNAME)).isTrue();
    }

    @Test
    void existsByUsernameReturnsFalseWhenUserDoesNotExist() {
        assertThat(userRepository.existsByUsername(UNKNOWN_VALUE)).isFalse();
    }

    @Test
    void existsByEmailReturnsTrueWhenUserExists() {
        userRepository.save(baseUser().build());
        entityManager.flush();

        assertThat(userRepository.existsByEmail(EMAIL)).isTrue();
    }

    @Test
    void existsByEmailReturnsFalseWhenUserDoesNotExist() {
        assertThat(userRepository.existsByEmail(UNKNOWN_VALUE)).isFalse();
    }

    @Test
    void savingUserWithoutPasswordViolatesNotNullConstraint() {
        var user = User.builder()
                .username(USERNAME)
                .email(EMAIL)
                .build();

        assertThatThrownBy(() -> {
            userRepository.save(user);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void savingUserWithDuplicateUsernameViolatesUniqueConstraint() {
        userRepository.save(baseUser().build());
        entityManager.flush();

        assertThatThrownBy(() -> {
            userRepository.save(User.builder()
                    .username(USERNAME)
                    .email(EMAIL_SECONDARY)
                    .password(PASSWORD)
                    .build());
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void savingUserWithDuplicateEmailViolatesUniqueConstraint() {
        userRepository.save(baseUser().build());
        entityManager.flush();

        assertThatThrownBy(() -> {
            userRepository.save(User.builder()
                    .username(USERNAME_SECONDARY)
                    .email(EMAIL)
                    .password(PASSWORD)
                    .build());
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}

