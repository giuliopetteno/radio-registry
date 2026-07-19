package com.gp.radioregistry.security.userdetails;

import com.gp.radioregistry.user.domain.User;
import com.gp.radioregistry.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService unit tests")
class CustomUserDetailsServiceTest {

    private static final String USERNAME = "USER";
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "right_password";

    private static final String USERNAME_WRONG = "wrong_user";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username(USERNAME)
                .email(EMAIL)
                .password(PASSWORD)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("should return the user details when found by username")
    void loadUserByUsername_foundByUsername() {
        when(userRepository.findByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        assertSame(user, result);
        assertEquals(USERNAME, result.getUsername());
        verify(userRepository).findByUsernameOrEmail(USERNAME, USERNAME);
    }

    @Test
    @DisplayName("should query by the same value for both username and email arguments")
    void loadUserByUsername_foundByEmail() {
        when(userRepository.findByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        assertSame(user, result);
        verify(userRepository).findByUsernameOrEmail(USERNAME, USERNAME);
    }

    @Test
    @DisplayName("should throw UsernameNotFoundException when no user matches")
    void loadUserByUsername_notFound() {
        when(userRepository.findByUsernameOrEmail(USERNAME_WRONG, USERNAME_WRONG)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(USERNAME_WRONG));
        verify(userRepository).findByUsernameOrEmail(USERNAME_WRONG, USERNAME_WRONG);
    }
}

