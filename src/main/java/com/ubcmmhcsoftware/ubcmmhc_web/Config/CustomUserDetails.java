package com.ubcmmhcsoftware.ubcmmhc_web.Config;


import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;


public class CustomUserDetails implements UserDetails {
    private UUID id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.authorities = user.getUser_roles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().toString()))
                .toList();
    }

    public UUID getId() { return id; }

    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
