package com.gp.radioregistry.security.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.AuthorityAuthorizationDecision;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthorizationEventListener {
    @EventListener
    public void onFailure(AuthorizationDeniedEvent deniedEvent) {
        if (!(deniedEvent.getAuthorizationResult() instanceof AuthorityAuthorizationDecision)) {
            return;
        }
        log.error("Authorization failed for the user: {} due to: {}", deniedEvent.getAuthentication().get().getName(),
            deniedEvent.getAuthorizationResult());
    }
}
