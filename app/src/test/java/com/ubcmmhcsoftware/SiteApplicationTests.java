package com.ubcmmhcsoftware;

import com.ubcmmhcsoftware.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SiteApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void userInfoWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginAndFetchProfile() throws Exception {
        String email = "flow-" + System.nanoTime() + "@mmhc.test";
        mockMvc.perform(post("/api/auth/register-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password123!"}
                                """.formatted(email)))
                .andExpect(status().isOk());

        MvcResult login = mockMvc.perform(post("/api/auth/login-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password123!"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        jakarta.servlet.http.Cookie jwt = login.getResponse().getCookie("JWT");
        assertThat(jwt).isNotNull();
        assertThat(jwt.getValue()).isNotBlank();

        mockMvc.perform(get("/api/user/info").cookie(jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void duplicateRegisterReturnsConflict() throws Exception {
        String email = "dup-" + System.nanoTime() + "@mmhc.test";
        String body = """
                {"email":"%s","password":"Password123!"}
                """.formatted(email);
        mockMvc.perform(post("/api/auth/register-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/register-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void newsletterSubscribePersistsEvenIfBrevoIsUnreachable() throws Exception {
        String email = "news-" + System.nanoTime() + "@mmhc.test";
        mockMvc.perform(post("/api/newsletter/add-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isOk());

        assertThat(newsletterRepository.findByEmail(email)).isPresent();
    }

    @Test
    void cashMembershipRegisterAndPublicCheck() throws Exception {
        String email = "member-" + System.nanoTime() + "@mmhc.test";
        mockMvc.perform(post("/api/membership/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Test Member",
                                  "email": "%s",
                                  "membershipType": "UBC_STUDENT",
                                  "paymentMethod": "CASH",
                                  "newsletterOptIn": true
                                }
                                """.formatted(email)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/membership/check").param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/membership/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void membershipStatusRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/membership/my-status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminRoutesRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/memberships/pending"))
                .andExpect(status().isUnauthorized());
    }
}
