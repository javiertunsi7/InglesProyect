package com.englishlearning.security;

import com.englishlearning.domain.enums.Role;

import java.security.Principal;

/**
 * Lightweight principal stored in the SecurityContext. Avoids dragging the
 * JPA entity into the request scope and keeps controllers easy to inject.
 */
public record AuthenticatedUser(Long id, String email, String displayName, Role role) implements Principal {

    @Override
    public String getName() {
        return email;
    }
}
