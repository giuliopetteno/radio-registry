package com.gp.radioregistry.security.auth.controller;

import com.gp.radioregistry.security.auth.dto.TokensDTO;
import com.gp.radioregistry.security.auth.dto.request.LoginRequest;
import com.gp.radioregistry.security.auth.dto.request.RegisterUserRequest;
import com.gp.radioregistry.security.auth.dto.response.AuthResponse;
import com.gp.radioregistry.security.auth.service.AuthenticationService;
import com.gp.radioregistry.user.domain.User;
import com.gp.radioregistry.user.dto.response.UserResponse;
import com.gp.radioregistry.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URI;
import java.time.OffsetDateTime;

import static com.gp.radioregistry.constant.ApiConstants.USERS_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController unit tests")
class AuthenticationControllerTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "username";
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "right_password";
    private static final String PASSWORD_WRONG = "wrong_password";

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .password(PASSWORD)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("registerUser")
    class RegisterUser {

        @Test
        @DisplayName("should return 201 Created with location header and mapped user")
        void registerUser_returnsCreated() {
            var request = new RegisterUserRequest(USERNAME, EMAIL, PASSWORD);
            when(userService.createUser(request)).thenReturn(user);

            ResponseEntity<UserResponse> response = authenticationController.registerUser(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(URI.create(USERS_PATH + "/" + USER_ID), response.getHeaders().getLocation());
            assertNotNull(response.getBody());
            assertEquals(USER_ID, response.getBody().id());
            assertEquals(USERNAME, response.getBody().username());
            verify(userService).createUser(request);
        }

        @Test
        @DisplayName("should propagate exceptions thrown by the service")
        void registerUser_propagatesServiceException() {
            var request = new RegisterUserRequest(USERNAME, EMAIL, PASSWORD);
            when(userService.createUser(request)).thenThrow(new RuntimeException("username exists"));

            assertThrows(RuntimeException.class, () -> authenticationController.registerUser(request));
        }
    }

    @Nested
    @DisplayName("doLogin")
    class DoLogin {

        @Test
        @DisplayName("should authenticate and return 200 OK with user")
        void doLogin_returnsOk() {
            var loginRequest = new LoginRequest(USERNAME, PASSWORD);
            Authentication authentication = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);
            var tokensDTO = new TokensDTO("access-token-value", "refresh-token-value", 900L, user);

            HttpServletRequest servletRequest = mock(HttpServletRequest.class);
            HttpSession session = mock(HttpSession.class);

            when(authenticationService.doAuthentication(loginRequest)).thenReturn(authentication);
            when(authenticationService.generateTokens(authentication)).thenReturn(tokensDTO);

            ResponseEntity<AuthResponse> response = authenticationController.doLogin(loginRequest);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().user());
            assertEquals(USER_ID, response.getBody().user().id());
            assertEquals(USERNAME, response.getBody().user().username());
            assertNotNull(response.getBody().loginTime());

            verify(authenticationService).doAuthentication(loginRequest);
            verify(authenticationService).generateTokens(authentication);
        }

        @Test
        @DisplayName("should propagate BadCredentialsException when authentication fails")
        void doLogin_badCredentials() {
            var loginRequest = new LoginRequest(USERNAME, PASSWORD_WRONG);
            HttpServletRequest servletRequest = mock(HttpServletRequest.class);
            when(authenticationService.doAuthentication(loginRequest))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class,
                    () -> authenticationController.doLogin(loginRequest));
            verify(userService, never()).findByUsernameOrEmail(any());
        }
    }
}

