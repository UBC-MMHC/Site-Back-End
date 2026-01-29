package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.CheckoutSessionDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.MembershipRegistrationDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.MembershipType;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.PaymentMethod;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.MembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
        UUID membershipId = UUID.randomUUID();
        String customerId = "cus_123";
        String subscriptionId = "sub_123";

        Membership pendingMembership = Membership.builder()
                .id(membershipId)
                .email("test@example.com")
                .paymentStatus("pending")
                .active(false)
                .build();

        when(membershipRepository.findById(membershipId)).thenReturn(Optional.of(pendingMembership));

        membershipService.activateMembership(membershipId.toString(), customerId, subscriptionId);

        ArgumentCaptor<Membership> captor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(captor.capture());

        Membership activated = captor.getValue();
        assertTrue(activated.isActive());
        assertEquals("completed", activated.getPaymentStatus());
        assertEquals(customerId, activated.getStripeCustomerId());
        assertEquals(subscriptionId, activated.getStripeSubscriptionId());
        assertNotNull(activated.getVerifiedAt());
        assertNotNull(activated.getEndDate());
    }

    @Test
    void activateMembership_withNoMembershipFound_shouldNotThrow() {
        UUID unknownId = UUID.randomUUID();
        when(membershipRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> membershipService.activateMembership(unknownId.toString(), "cus", "sub"));
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

    // Tests for manuallyApproveMembership

    @Test
    void manuallyApproveMembership_shouldActivateMembership() {
        String memberEmail = "member@example.com";
        String adminEmail = "admin@example.com";
        PaymentMethod paymentMethod = PaymentMethod.CASH;

        Membership pendingMembership = Membership.builder()
                .id(UUID.randomUUID())
                .email(memberEmail)
                .fullName("Test Member")
                .membershipType(MembershipType.UBC_STUDENT)
                .paymentStatus("pending")
                .active(false)
                .build();

        when(membershipRepository.findByEmail(memberEmail)).thenReturn(Optional.of(pendingMembership));

        membershipService.manuallyApproveMembership(memberEmail, paymentMethod, adminEmail);

        ArgumentCaptor<Membership> captor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(captor.capture());

        Membership approved = captor.getValue();
        assertTrue(approved.isActive());
        assertEquals("completed", approved.getPaymentStatus());
        assertEquals(PaymentMethod.CASH, approved.getPaymentMethod());
        assertEquals(adminEmail, approved.getApprovedBy());
        assertNotNull(approved.getVerifiedAt());
        assertNotNull(approved.getEndDate());
    }

    @Test
    void manuallyApproveMembership_withNoMembershipFound_shouldThrowException() {
        when(membershipRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> membershipService.manuallyApproveMembership("unknown@example.com", PaymentMethod.CASH,
                        "admin@example.com"));

        assertTrue(exception.getMessage().contains("No membership found"));
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void manuallyApproveMembership_alreadyActive_shouldThrowException() {
        Membership activeMembership = Membership.builder()
                .id(UUID.randomUUID())
                .email("active@example.com")
                .active(true)
                .build();

        when(membershipRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeMembership));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> membershipService.manuallyApproveMembership("active@example.com", PaymentMethod.ETRANSFER,
                        "admin@example.com"));

        assertEquals("Membership is already active", exception.getMessage());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void getPendingMemberships_shouldReturnPendingList() {
        Membership pending1 = Membership.builder()
                .id(UUID.randomUUID())
                .email("pending1@example.com")
                .active(false)
                .paymentStatus("pending")
                .build();

        Membership pending2 = Membership.builder()
                .id(UUID.randomUUID())
                .email("pending2@example.com")
                .active(false)
                .paymentStatus("pending")
                .build();

        when(membershipRepository.findByActiveAndPaymentStatus(false, "pending"))
                .thenReturn(List.of(pending1, pending2));

        List<Membership> result = membershipService.getPendingMemberships();

        assertEquals(2, result.size());
        verify(membershipRepository).findByActiveAndPaymentStatus(false, "pending");
    }
}
