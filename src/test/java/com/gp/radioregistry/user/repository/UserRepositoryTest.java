package com.gp.radioregistry.user.repository;

import com.gp.radioregistry.base.AbstractPostgresContainerTest;
import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.user.domain.User;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("should persist user and generate id")
    void savePersistsUserAndGeneratesId() {
        var saved = userRepository.save(baseUser().build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("should return persisted user by id")
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
    @DisplayName("should return empty when user does not exist")
    void findByIdReturnsEmptyWhenUserDoesNotExist() {
        Optional<User> found = userRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should return all persisted users")
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
    @DisplayName("should return the number of users")
    void countReturnsNumberOfUsers() {
        userRepository.save(baseUser().build());

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should remove the user")
    void deleteRemovesUser() {
        var saved = userRepository.save(baseUser().build());
        entityManager.flush();

        userRepository.delete(saved);
        entityManager.flush();

        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("should persist role associations")
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
    @DisplayName("should return matching user by username")
    void findByUsernameReturnsMatchingUser() {
        userRepository.save(baseUser().build());
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByUsername(USERNAME);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("should return empty when no user matches the username")
    void findByUsernameReturnsEmptyWhenNoUserMatches() {
        assertThat(userRepository.findByUsername(UNKNOWN_VALUE)).isEmpty();
    }

    @Test
    @DisplayName("should return matching user by email")
    void findByEmailReturnsMatchingUser() {
        userRepository.save(baseUser().build());
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByEmail(EMAIL);

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("should return empty when no user matches the email")
    void findByEmailReturnsEmptyWhenNoUserMatches() {
        assertThat(userRepository.findByEmail(UNKNOWN_VALUE)).isEmpty();
    }

    @Test
    @DisplayName("should match user by username with findByUsernameOrEmail")
    void findByUsernameOrEmailMatchesByUsername() {
        userRepository.save(baseUser().build());
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByUsernameOrEmail(USERNAME, UNKNOWN_VALUE);

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("should match user by email with findByUsernameOrEmail")
    void findByUsernameOrEmailMatchesByEmail() {
        userRepository.save(baseUser().build());
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByUsernameOrEmail(UNKNOWN_VALUE, EMAIL);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("should return empty when no user matches username or email")
    void findByUsernameOrEmailReturnsEmptyWhenNoUserMatches() {
        assertThat(userRepository.findByUsernameOrEmail(UNKNOWN_VALUE, UNKNOWN_VALUE)).isEmpty();
    }

    @Test
    @DisplayName("should return true when user exists by username")
    void existsByUsernameReturnsTrueWhenUserExists() {
        userRepository.save(baseUser().build());
        entityManager.flush();

        assertThat(userRepository.existsByUsername(USERNAME)).isTrue();
    }

    @Test
    @DisplayName("should return false when user does not exist by username")
    void existsByUsernameReturnsFalseWhenUserDoesNotExist() {
        assertThat(userRepository.existsByUsername(UNKNOWN_VALUE)).isFalse();
    }

    @Test
    @DisplayName("should return true when user exists by email")
    void existsByEmailReturnsTrueWhenUserExists() {
        userRepository.save(baseUser().build());
        entityManager.flush();

        assertThat(userRepository.existsByEmail(EMAIL)).isTrue();
    }

    @Test
    @DisplayName("should return false when user does not exist by email")
    void existsByEmailReturnsFalseWhenUserDoesNotExist() {
        assertThat(userRepository.existsByEmail(UNKNOWN_VALUE)).isFalse();
    }

    @Test
    @DisplayName("should violate not-null constraint when saving user without password")
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
    @DisplayName("should violate unique constraint when saving user with duplicate username")
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
    @DisplayName("should violate unique constraint when saving user with duplicate email")
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
