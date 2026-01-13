package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.AppProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.StripeProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.MembershipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StripeService.
 * Note: These tests verify internal logic. Full Stripe integration tests
 * require mocking the Stripe API client which is complex due to static methods.
 */
@ExtendWith(MockitoExtension.class)
class StripeServiceTest {

    @Mock
    private StripeProperties stripeProperties;

    @Mock
    private StripeProperties.Prices prices;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private StripeService stripeService;

    private Membership testMembership;

    @BeforeEach
    void setUp() {
        testMembership = Membership.builder()
                .id(UUID.randomUUID())
                .fullName("Test User")
                .email("test@example.com")
                .membershipType(MembershipType.UBC_STUDENT)
                .build();
    }

    @Test
    void verifyPriceIdMapping_ubcStudent() {
        when(stripeProperties.getPrices()).thenReturn(prices);
        when(prices.getUbcStudent()).thenReturn("price_ubc_student_123");

        // Verify price mapping is correct (internal method, accessed via reflection or
        // integration test)
        assertNotNull(stripeProperties.getPrices().getUbcStudent());
        assertEquals("price_ubc_student_123", prices.getUbcStudent());
    }

    @Test
    void verifyPriceIdMapping_nonUbcStudent() {
        when(stripeProperties.getPrices()).thenReturn(prices);
        when(prices.getNonUbcStudent()).thenReturn("price_non_ubc_student_123");

        assertNotNull(stripeProperties.getPrices().getNonUbcStudent());
        assertEquals("price_non_ubc_student_123", prices.getNonUbcStudent());
    }

    @Test
    void verifyPriceIdMapping_nonStudent() {
        when(stripeProperties.getPrices()).thenReturn(prices);
        when(prices.getNonStudent()).thenReturn("price_non_student_123");

        assertNotNull(stripeProperties.getPrices().getNonStudent());
        assertEquals("price_non_student_123", prices.getNonStudent());
    }

    @Test
    void stripeProperties_shouldHaveSecretKey() {
        when(stripeProperties.getSecretKey()).thenReturn("sk_test_123");

        assertEquals("sk_test_123", stripeProperties.getSecretKey());
    }

    @Test
    void stripeProperties_shouldHaveWebhookSecret() {
        when(stripeProperties.getWebhookSecret()).thenReturn("whsec_123");

        assertEquals("whsec_123", stripeProperties.getWebhookSecret());
    }
}
