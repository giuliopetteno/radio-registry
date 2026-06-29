package com.gp.radioregistry.security.auth.service;

import com.gp.radioregistry.security.auth.dto.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    public final AuthenticationManager authenticationManager;

    public Authentication doAuthentication(LoginRequest loginRequest) {
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
    }
}