package com.gp.radioregistry.security.listener;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.AuthorityAuthorizationDecision;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

@Log4j2
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
