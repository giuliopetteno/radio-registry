package com.gp.radioregistry.security.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp.radioregistry.exception.ResourceAlreadyExistsException;
import com.gp.radioregistry.security.auth.dto.request.LoginRequest;
import com.gp.radioregistry.security.auth.dto.request.RegisterUserRequest;
import com.gp.radioregistry.security.auth.service.AuthenticationService;
import com.gp.radioregistry.security.config.SecurityConfig;
import com.gp.radioregistry.user.domain.User;
import com.gp.radioregistry.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static com.gp.radioregistry.constant.ApiConstants.AUTH_PATH;
import static com.gp.radioregistry.constant.ApiConstants.USERS_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthenticationController @WebMvcTest")
class AuthenticationControllerWebMvcTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "john.doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String PASSWORD = "Str0ngPass1";

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();

        OffsetDateTime now = OffsetDateTime.now();

        user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .password("encoded-password")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private RegisterUserRequest validRegisterRequest() {
        return new RegisterUserRequest(USERNAME, EMAIL, PASSWORD);
    }

    private LoginRequest validLoginRequest() {
        return new LoginRequest(USERNAME, PASSWORD);
    }

    @Nested
    @DisplayName("Security (public endpoints)")
    class Security {

        @Test
        @DisplayName("POST /register is accessible without authentication")
        void registerIsPublic() throws Exception {
            when(userService.createUser(any(RegisterUserRequest.class))).thenReturn(user);

            mockMvc.perform(post(AUTH_PATH + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("POST /login is accessible without authentication")
        void loginIsPublic() throws Exception {
            Authentication authentication = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);
            when(authenticationService.doAuthentication(any(LoginRequest.class))).thenReturn(authentication);
            when(userService.findByUsernameOrEmail(USERNAME)).thenReturn(user);

            mockMvc.perform(post(AUTH_PATH + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest())))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Bean validation")
    class Validation {

        @Test
        @DisplayName("POST /register returns 400 with field errors when required fields are missing")
        void registerBlankFieldsReturns400() throws Exception {
            var invalid = new RegisterUserRequest("  ", "not-an-email", "");

            mockMvc.perform(post(AUTH_PATH + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.username").exists())
                    .andExpect(jsonPath("$.errors.email").exists())
                    .andExpect(jsonPath("$.errors.password").exists());
        }

        @Test
        @DisplayName("POST /register returns 400 when email is invalid")
        void registerInvalidEmailReturns400() throws Exception {
            var invalid = new RegisterUserRequest(USERNAME, "invalid-email", PASSWORD);

            mockMvc.perform(post(AUTH_PATH + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists());
        }

        @Test
        @DisplayName("POST /login returns 400 when username and password are blank")
        void loginBlankFieldsReturns400() throws Exception {
            var invalid = new LoginRequest("  ", "");

            mockMvc.perform(post(AUTH_PATH + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.username").exists())
                    .andExpect(jsonPath("$.errors.password").exists());
        }
    }

    @Nested
    @DisplayName("Exception mapping")
    class ExceptionMapping {

        @Test
        @DisplayName("POST /register maps ResourceAlreadyExistsException to 409 ProblemDetail")
        void registerDuplicateReturns409() throws Exception {
            when(userService.createUser(any(RegisterUserRequest.class)))
                    .thenThrow(new ResourceAlreadyExistsException("User with username " + USERNAME + " already exists"));

            mockMvc.perform(post(AUTH_PATH + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest())))
                    .andExpect(status().isConflict())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.title").value("Resource already exists"))
                    .andExpect(jsonPath("$.detail").value("User with username " + USERNAME + " already exists"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("JSON response shape & headers")
    class ResponseShape {

        @Test
        @DisplayName("POST /register returns 201 with Location header and AuthResponse body")
        void registerReturns201WithLocationAndBody() throws Exception {
            when(userService.createUser(any(RegisterUserRequest.class))).thenReturn(user);

            mockMvc.perform(post(AUTH_PATH + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", USERS_PATH + "/" + USER_ID))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.user.id").value(USER_ID))
                    .andExpect(jsonPath("$.user.username").value(USERNAME))
                    .andExpect(jsonPath("$.user.email").value(EMAIL))
                    .andExpect(jsonPath("$.user.password").doesNotExist())
                    .andExpect(jsonPath("$.loginTime").exists());
        }

        @Test
        @DisplayName("POST /login returns 200 with AuthResponse body and stores the security context")
        void loginReturns200WithBody() throws Exception {
            Authentication authentication = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);
            when(authenticationService.doAuthentication(any(LoginRequest.class))).thenReturn(authentication);
            when(userService.findByUsernameOrEmail(USERNAME)).thenReturn(user);

            mockMvc.perform(post(AUTH_PATH + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.user.id").value(USER_ID))
                    .andExpect(jsonPath("$.user.username").value(USERNAME))
                    .andExpect(jsonPath("$.user.password").doesNotExist())
                    .andExpect(jsonPath("$.loginTime").exists());

            verify(authenticationService).doAuthentication(any(LoginRequest.class));
            verify(userService).findByUsernameOrEmail(USERNAME);
        }

        @Test
        @DisplayName("POST /login propagates BadCredentialsException (no user lookup)")
        void loginBadCredentials() throws Exception {
            when(authenticationService.doAuthentication(any(LoginRequest.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post(AUTH_PATH + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest())))
                    .andExpect(status().is4xxClientError());

            verify(userService, never()).findByUsernameOrEmail(any());
        }
    }
}

