package com.gp.radioregistry.security.listener;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class AuthenticationEventListener {
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent successEvent) {
        log.info("Login successful for the user: {}", successEvent.getAuthentication().getName());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failureEvent) {
        log.error("Login failed for the user: {} due to: {}", failureEvent.getAuthentication().getName(),
                failureEvent.getException().getMessage());
    }

}
