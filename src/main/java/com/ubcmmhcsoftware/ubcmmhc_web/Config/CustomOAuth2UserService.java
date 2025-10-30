package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final  UserRepository userRepository;

    // in future if we want to use/add github auth along with google we can implmement here
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String email;
        String name;
        String providerId;

        if ("google".equals(registrationId)) {
            email = oauthUser.getAttribute("email");
            name = oauthUser.getAttribute("name");
            providerId = oauthUser.getAttribute("sub");
        } else {
            name = null;
            email = null;
            providerId = null;
        }

        if (email != null) {
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setRole("USER");
                return newUser;
            });

            if ("google".equals(registrationId)) {
                user.setGoogleId(providerId);
            }

            userRepository.save(user);
        }

        return oauthUser;
    }
}
