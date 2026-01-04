package com.ubcmmhcsoftware.ubcmmhc_web.ControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubcmmhcsoftware.ubcmmhc_web.Controller.NewsletterController;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.SubscribeToNewsletterRequest;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.NewsletterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsletterController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NewsletterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsletterService newsletterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addEmail_Success() throws Exception {
        SubscribeToNewsletterRequest request = new SubscribeToNewsletterRequest();
        request.setEmail("test@example.com");

        doNothing().when(newsletterService).addEmail("test@example.com");

        mockMvc.perform(post("/api/newsletter/add-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email subscribed to the newsletter"));

        verify(newsletterService, times(1)).addEmail("test@example.com");
    }

    @Test
    void addEmail_AlreadySubscribed_Returns409() throws Exception {
        SubscribeToNewsletterRequest request = new SubscribeToNewsletterRequest();
        request.setEmail("existing@example.com");

        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email is already subscribed"))
                .when(newsletterService).addEmail("existing@example.com");

        mockMvc.perform(post("/api/newsletter/add-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(newsletterService, times(1)).addEmail("existing@example.com");
    }
}
