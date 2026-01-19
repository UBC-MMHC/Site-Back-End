package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.CheckoutSessionDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.MembershipRegistrationDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.MembershipType;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.MembershipRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.RoleRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MembershipService.
 */
@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private StripeService stripeService;

    @Mock
    private NewsletterService newsletterService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MembershipService membershipService;

    private MembershipRegistrationDTO testDTO;
    private Membership savedMembership;
    private Session mockSession;

    @BeforeEach
    void setUp() {
        testDTO = MembershipRegistrationDTO.builder()
                .fullName("Test User")
                .email("test@example.com")
                .membershipType(MembershipType.UBC_STUDENT)
                .studentId("12345678")
                .instagram("@testuser")
                .instagramGroupchat(true)
                .newsletterOptIn(false)
                .build();

        savedMembership = Membership.builder()
                .id(UUID.randomUUID())
                .fullName("Test User")
                .email("test@example.com")
                .membershipType(MembershipType.UBC_STUDENT)
                .studentId("12345678")
                .instagram("@testuser")
                .instagramGroupchat(true)
                .newsletterOptIn(false)
                .paymentStatus("pending")
                .active(false)
                .build();

        mockSession = mock(Session.class);
    }

    @Test
    void createMembership_shouldSaveMembershipWithPendingStatus() throws StripeException {
        when(membershipRepository.existsByEmail(testDTO.getEmail())).thenReturn(false);
        when(membershipRepository.save(any(Membership.class))).thenReturn(savedMembership);
        when(stripeService.createCheckoutSession(any(Membership.class))).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn("cs_test_123");
        when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/c/pay/cs_test_123");

        CheckoutSessionDTO result = membershipService.createMembership(testDTO);

        assertNotNull(result);
        assertEquals("cs_test_123", result.getSessionId());
        assertEquals("https://checkout.stripe.com/c/pay/cs_test_123", result.getSessionUrl());

        ArgumentCaptor<Membership> membershipCaptor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository, times(2)).save(membershipCaptor.capture());

        Membership captured = membershipCaptor.getAllValues().get(0);
        assertEquals("pending", captured.getPaymentStatus());
        assertFalse(captured.isActive());
    }

    @Test
    void createMembership_shouldThrowExceptionIfEmailExists() {
        when(membershipRepository.existsByEmail(testDTO.getEmail())).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> membershipService.createMembership(testDTO));

        assertEquals("A membership already exists for this email", exception.getMessage());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void createMembership_withNewsletterOptIn_shouldCallNewsletterService() throws StripeException {
        testDTO.setNewsletterOptIn(true);
        when(membershipRepository.existsByEmail(testDTO.getEmail())).thenReturn(false);
        when(membershipRepository.save(any(Membership.class))).thenReturn(savedMembership);
        when(stripeService.createCheckoutSession(any(Membership.class))).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn("cs_test_123");
        when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/c/pay/cs_test_123");

        membershipService.createMembership(testDTO);

        verify(newsletterService).addEmail(testDTO.getEmail());
    }

    @Test
    void createMembership_newsletterFailure_shouldNotThrow() throws StripeException {
        testDTO.setNewsletterOptIn(true);
        when(membershipRepository.existsByEmail(testDTO.getEmail())).thenReturn(false);
        when(membershipRepository.save(any(Membership.class))).thenReturn(savedMembership);
        when(stripeService.createCheckoutSession(any(Membership.class))).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn("cs_test_123");
        when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/c/pay/cs_test_123");
        doThrow(new RuntimeException("Newsletter failure")).when(newsletterService).addEmail(any());

        // Should not throw even if newsletter fails
        assertDoesNotThrow(() -> membershipService.createMembership(testDTO));
    }

    @Test
    void activateMembership_shouldSetActiveAndVerifiedAt() {
        String sessionId = "cs_test_123";
        String customerId = "cus_123";

        Membership pendingMembership = Membership.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .stripeSessionId(sessionId)
                .paymentStatus("pending")
                .active(false)
                .build();

        when(membershipRepository.findByStripeSessionId(sessionId)).thenReturn(Optional.of(pendingMembership));

        membershipService.activateMembership(sessionId, customerId);

        ArgumentCaptor<Membership> captor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(captor.capture());

        Membership activated = captor.getValue();
        assertTrue(activated.isActive());
        assertEquals("completed", activated.getPaymentStatus());
        assertEquals(customerId, activated.getStripeCustomerId());
        assertNotNull(activated.getVerifiedAt());
        assertNotNull(activated.getEndDate());
    }

    @Test
    void activateMembership_withNoMembershipFound_shouldNotThrow() {
        when(membershipRepository.findByStripeSessionId(any())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> membershipService.activateMembership("unknown", "cus"));
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void getMembershipByEmail_shouldReturnMembership() {
        when(membershipRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedMembership));

        Optional<Membership> result = membershipService.getMembershipByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void hasActiveMembership_shouldReturnTrueForActiveMember() {
        savedMembership.setActive(true);
        when(membershipRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedMembership));

        assertTrue(membershipService.hasActiveMembership("test@example.com"));
    }

    @Test
    void hasActiveMembership_shouldReturnFalseForInactiveMember() {
        savedMembership.setActive(false);
        when(membershipRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedMembership));

        assertFalse(membershipService.hasActiveMembership("test@example.com"));
    }

    @Test
    void hasActiveMembership_shouldReturnFalseForNonExistentMember() {
        when(membershipRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertFalse(membershipService.hasActiveMembership("nonexistent@example.com"));
    }
}
