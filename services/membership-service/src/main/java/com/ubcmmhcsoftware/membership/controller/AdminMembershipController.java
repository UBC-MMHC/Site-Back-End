package com.ubcmmhcsoftware.membership.controller;

import com.ubcmmhcsoftware.membership.entity.Membership;
import com.ubcmmhcsoftware.membership.enums.PaymentMethod;
import com.ubcmmhcsoftware.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/memberships")
@Slf4j
public class AdminMembershipController {

    private final MembershipService membershipService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingMemberships() {
        List<Membership> pending = membershipService.getPendingMemberships();
        return ResponseEntity.ok(pending.stream().map(m -> Map.of(
                "email", m.getEmail(),
                "fullName", m.getFullName(),
                "membershipType", m.getMembershipType().name(),
                "studentId", m.getStudentId() != null ? m.getStudentId() : "",
                "instagram", m.getInstagram() != null ? m.getInstagram() : "",
                "paymentStatus", m.getPaymentStatus() != null ? m.getPaymentStatus() : "pending")).toList());
    }

    @PostMapping("/{memberEmail}/approve")
    public ResponseEntity<?> approveMembership(
            @PathVariable String memberEmail,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal UserDetails adminDetails) {

        if (adminDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String paymentMethodStr = requestBody.get("paymentMethod");
        if (paymentMethodStr == null || paymentMethodStr.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "paymentMethod is required (CASH, ETRANSFER, or OTHER)"));
        }

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid paymentMethod. Must be CASH, ETRANSFER, or OTHER"));
        }

        if (paymentMethod == PaymentMethod.STRIPE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "STRIPE payments should be processed through the webhook, not manually approved"));
        }

        try {
            membershipService.manuallyApproveMembership(memberEmail, paymentMethod, adminDetails.getUsername());
            log.info("Admin {} approved membership for {} via {}",
                    adminDetails.getUsername(), memberEmail, paymentMethod);
            return ResponseEntity.ok(Map.of(
                    "message", "Membership approved successfully",
                    "memberEmail", memberEmail,
                    "paymentMethod", paymentMethod.name(),
                    "approvedBy", adminDetails.getUsername()));
        } catch (IllegalStateException e) {
            log.warn("Failed to approve membership for {}: {}", memberEmail, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
