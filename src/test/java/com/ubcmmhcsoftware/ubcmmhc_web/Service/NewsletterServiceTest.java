package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.NewsletterSubscriber;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.NewsletterRepository;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceTest {

    @Mock
    private NewsletterRepository newsletterRepository;

    @InjectMocks
    private NewsletterService newsletterService;

    @BeforeEach
    void setUp() {
        // Set the brevoApiKey using reflection since it's @Value injected
        ReflectionTestUtils.setField(newsletterService, "brevoApiKey", "test-api-key");
        ReflectionTestUtils.setField(newsletterService, "newsletterListId", 2);
        ReflectionTestUtils.setField(newsletterService, "brevoBaseUrl", "https://api.brevo.com/v3");
    }

    @Test
    @DisplayName("Should throw 409 Conflict when email is already subscribed")
    void addEmail_AlreadySubscribed_ShouldThrowConflict() {
        String email = "existing@example.com";
        NewsletterSubscriber existingSubscriber = NewsletterSubscriber.builder()
                .email(email)
                .unsubscribed(false)
                .build();

        when(newsletterRepository.findByEmail(email)).thenReturn(existingSubscriber);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            newsletterService.addEmail(email);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("already subscribed"));
    }

    @Test
    @DisplayName("Should allow resubscription for previously unsubscribed email")
    void addEmail_UnsubscribedEmail_ShouldNotThrowConflict() {
        String email = "unsubscribed@example.com";
        NewsletterSubscriber unsubscribedUser = NewsletterSubscriber.builder()
                .email(email)
                .unsubscribed(true)
                .build();

        when(newsletterRepository.findByEmail(email)).thenReturn(unsubscribedUser);

        // This will throw RestClientException due to no actual API,
        // but we're testing the conflict logic doesn't trigger
        try {
            newsletterService.addEmail(email);
        } catch (Exception e) {
            // Expected - API call fails in test, but conflict check passed
            assertFalse(e instanceof ResponseStatusException);
        }
    }

    @Test
    @DisplayName("Should not throw conflict when email is new")
    void addEmail_NewEmail_ShouldNotThrowConflict() {
        String email = "new@example.com";

        when(newsletterRepository.findByEmail(email)).thenReturn(null);

        // The method will fail at REST call, but we verify no conflict exception
        try {
            newsletterService.addEmail(email);
        } catch (Exception e) {
            // Expected - API call fails in test, but conflict check passed
            assertFalse(e instanceof ResponseStatusException);
        }

        // Verify the conflict check passed (no conflict thrown before REST call)
        verify(newsletterRepository).findByEmail(email);
    }
}
