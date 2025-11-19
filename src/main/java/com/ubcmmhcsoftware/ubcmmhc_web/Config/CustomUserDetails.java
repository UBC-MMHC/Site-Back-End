package com.ubcmmhcsoftware.ubcmmhc_web.Config;


import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {
    @Getter
    private UUID id;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getEmail();
        this.authorities = user.getUser_roles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().toString()))
                .toList();
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
        return "";
    }
}
