package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;

    // Views all information being sent from whatever authenticator
    @GetMapping("/info")
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> userInfo(OAuth2AuthenticationToken authentication) {
        return authentication.getPrincipal().getAttributes();
    }
}
