package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.PaymentMethod;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.RoleRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for admin operations.
 * All endpoints require ADMIN role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    private final MembershipService membershipService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Gets all pending (unpaid) memberships for admin review.
     *
     * @return List of pending memberships
     */
    @GetMapping("/memberships/pending")
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

    /**
     * Manually approves a membership for cash/e-transfer payments.
     *
     * @param memberEmail  The member's email (path variable)
     * @param requestBody  Contains paymentMethod (CASH, ETRANSFER, OTHER)
     * @param adminDetails The authenticated admin
     * @return Success message or error
     */
    @PostMapping("/memberships/{memberEmail}/approve")
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

    /**
     * Assigns a role to a user.
     */
    @PostMapping("/users/{userEmail}/role")
    public ResponseEntity<?> assignRole(
            @PathVariable String userEmail,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal UserDetails adminDetails) {

        if (adminDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String roleStr = requestBody.get("role");
        if (roleStr == null || roleStr.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "role is required (ROLE_USER, ROLE_ADMIN, or ROLE_SUPERADMIN)"));
        }

        RoleEnum roleEnum;
        try {
            roleEnum = RoleEnum.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role. Must be ROLE_USER, ROLE_ADMIN, or ROLE_SUPERADMIN"));
        }

        User user = userRepository.findUserByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found: " + userEmail));
        }

        Role role = roleRepository.findByName(roleEnum).orElse(null);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Role not found in database"));
        }

        if (user.getUser_roles().contains(role)) {
            return ResponseEntity.ok(Map.of("message", "User already has this role"));
        }

        user.getUser_roles().add(role);
        userRepository.save(user);
        log.info("Admin {} assigned role {} to user {}", adminDetails.getUsername(), roleEnum, userEmail);

        return ResponseEntity.ok(Map.of(
                "message", "Role assigned successfully",
                "userEmail", userEmail,
                "role", roleEnum.name()));
    }

    /**
     * Removes a role from a user.
     */
    @DeleteMapping("/users/{userEmail}/role")
    public ResponseEntity<?> removeRole(
            @PathVariable String userEmail,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal UserDetails adminDetails) {

        if (adminDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String roleStr = requestBody.get("role");
        if (roleStr == null || roleStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "role is required"));
        }

        RoleEnum roleEnum;
        try {
            roleEnum = RoleEnum.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        }

        User user = userRepository.findUserByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        Role role = roleRepository.findByName(roleEnum).orElse(null);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Role not found"));
        }

        user.getUser_roles().remove(role);
        userRepository.save(user);
        log.info("Admin {} removed role {} from user {}", adminDetails.getUsername(), roleEnum, userEmail);

        return ResponseEntity.ok(Map.of(
                "message", "Role removed successfully",
                "userEmail", userEmail,
                "role", roleEnum.name()));
    }
}
