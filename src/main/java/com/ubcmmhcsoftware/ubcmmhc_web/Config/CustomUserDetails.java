package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * The "Security Identity" of a user.
 * <p>
 * This class implements the Adapter Pattern. It wraps our custom database {@link User} entity
 * into the standard {@link UserDetails} interface required by Spring Security.
 * <br>
 * When Spring Security asks "Who is logged in?", it looks at this object.
 * </p>
 */
public class CustomUserDetails implements UserDetails {
    @Getter
    private final UUID id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructor: Converts a raw Database User into a Security User.
     * @param user The entity fetched from the repository.
     */
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.authorities = user.getUser_roles().stream().map(role -> new SimpleGrantedAuthority(role.getName().toString())).toList();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
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
}
