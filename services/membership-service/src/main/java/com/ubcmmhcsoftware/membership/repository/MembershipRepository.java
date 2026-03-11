package com.ubcmmhcsoftware.membership.repository;

import com.ubcmmhcsoftware.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    Optional<Membership> findByEmail(String email);

    Optional<Membership> findByStripeCustomerId(String stripeCustomerId);

    Optional<Membership> findByStripeSubscriptionId(String subscriptionId);

    Optional<Membership> findByStripeSessionId(String sessionId);

    boolean existsByEmail(String email);

    List<Membership> findByActiveAndPaymentStatus(boolean active, String paymentStatus);
}
