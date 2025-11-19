package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    public JWTAuthenticationFilter(JWTService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("token".equals(c.getName())) {
                    token = c.getValue();
                }
            }
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String id = jwtService.extractId(token);
            if (id != null && jwtService.isTokenValid(token, id)) {
                var user = userRepository.findUserByIdWithRoles(UUID.fromString(id));
                if (user.isPresent()) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
