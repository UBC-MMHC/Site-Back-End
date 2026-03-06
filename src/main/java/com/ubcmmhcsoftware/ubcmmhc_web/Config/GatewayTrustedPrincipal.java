package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * UserDetails implementation for requests that came through the gateway.
 * <p>
 * When the gateway validates the JWT, it forwards X-User-Id, X-User-Email, X-User-Roles.
 * This principal trusts those headers—no JWT validation in the backend.
 * </p>
 */
@Getter
public class GatewayTrustedPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    public GatewayTrustedPrincipal(String userId, String email, List<GrantedAuthority> authorities) {
        this.id = userId != null && !userId.isBlank() ? UUID.fromString(userId) : null;
        this.email = email != null ? email : "";
        this.authorities = authorities != null ? List.copyOf(authorities) : Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Parses X-User-Roles header (comma-separated, e.g. "ROLE_USER,ROLE_ADMIN") into GrantedAuthorities.
     */
    public static List<GrantedAuthority> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return Collections.emptyList();
        }
        return Stream.of(rolesHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r))
                .toList();
    }
}
