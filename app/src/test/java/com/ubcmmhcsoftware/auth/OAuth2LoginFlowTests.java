package com.ubcmmhcsoftware.auth;

import com.ubcmmhcsoftware.auth.config.MyOAuth2SuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Google sign-in redirects. Regression tests for issue #28 ("404 Upon Sign In").
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class OAuth2LoginFlowTests {

    private static final String FRONTEND_URL = "http://localhost:3000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MyOAuth2SuccessHandler successHandler;

    @Test
    void googleSignInStartRedirectsToGoogle() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://accounts.google.com/**"));
    }

    @Test
    void failedGoogleCallbackRedirectsToFrontendLoginInsteadOfBackend404() throws Exception {
        // A callback whose state doesn't match a stored authorization request fails, just like an
        // expired session or a rejected client secret. It used to go to the backend's /login?error.
        mockMvc.perform(get("/login/oauth2/code/google")
                        .param("code", "bogus-code")
                        .param("state", "unknown-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(FRONTEND_URL + "/login?error=oauth_failed"));

        mockMvc.perform(get("/login/oauth2/code/google")
                        .param("error", "access_denied")
                        .param("state", "unknown-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(FRONTEND_URL + "/login?error=oauth_failed"));
    }

    @Test
    void nonMemberGoogleSignInLandsOnFrontendCallbackWithWorkingToken() throws Exception {
        String email = "former-member-" + System.nanoTime() + "@mmhc.test";
        mockMvc.perform(post("/api/auth/register-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password123!"}
                                """.formatted(email)))
                .andExpect(status().isOk());

        DefaultOAuth2User googleUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                Map.of("sub", "google-" + System.nanoTime(), "email", email, "name", "Former Member"),
                "sub");
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                googleUser, googleUser.getAuthorities(), "google");

        MockHttpServletResponse response = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response, auth);

        String redirect = response.getRedirectedUrl();
        assertThat(redirect).startsWith(FRONTEND_URL + "/auth/callback?token=");
        String token = UriComponentsBuilder.fromUriString(redirect).build()
                .getQueryParams().getFirst("token");
        assertThat(token).isNotBlank();

        // No membership is not an error: the frontend's MembershipGate sends them to /membership.
        mockMvc.perform(get("/api/membership/my-status").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasMembership").value(false))
                .andExpect(jsonPath("$.isPaid").value(false));
    }
}
