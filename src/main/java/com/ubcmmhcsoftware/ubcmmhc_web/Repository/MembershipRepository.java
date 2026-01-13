package com.ubcmmhcsoftware.ubcmmhc_web.Repository;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Membership entity operations.
 */
@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    Optional<Membership> findByEmail(String email);

    Optional<Membership> findByStripeCustomerId(String stripeCustomerId);

    Optional<Membership> findByStripeSubscriptionId(String subscriptionId);

    Optional<Membership> findByStripeSessionId(String sessionId);

    boolean existsByEmail(String email);
}
