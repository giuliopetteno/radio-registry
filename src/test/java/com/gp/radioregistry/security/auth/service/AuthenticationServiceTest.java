package com.gp.radioregistry.security.auth.service;

import com.gp.radioregistry.security.auth.dto.request.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService unit tests")
class AuthenticationServiceTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "right_password";
    private static final String PASSWORD_WRONG = "wrong_password";

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("should delegate to the authentication manager and return the authentication")
    void doAuthentication_returnsAuthentication() {
        var loginRequest = new LoginRequest(USERNAME, PASSWORD);
        Authentication authentication = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        Authentication result = authenticationService.doAuthentication(loginRequest);

        assertSame(authentication, result);
        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals(USERNAME, captor.getValue().getPrincipal());
        assertEquals(PASSWORD, captor.getValue().getCredentials());
    }

    @Test
    @DisplayName("should propagate AuthenticationException when credentials are invalid")
    void doAuthentication_badCredentials() {
        var loginRequest = new LoginRequest(USERNAME, PASSWORD_WRONG);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authenticationService.doAuthentication(loginRequest));
    }
}

