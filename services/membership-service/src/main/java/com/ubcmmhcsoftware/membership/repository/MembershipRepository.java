package com.ubcmmhcsoftware.membership.repository;

import com.ubcmmhcsoftware.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    Optional<Membership> findByEmailIgnoreCase(String email);

    List<Membership> findAllByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Membership> findByStripeCustomerId(String stripeCustomerId);

    Optional<Membership> findByStripeSubscriptionId(String subscriptionId);

    Optional<Membership> findByStripeSessionId(String sessionId);

    List<Membership> findByActiveAndPaymentStatus(boolean active, String paymentStatus);
}
