package com.ubcmmhcsoftware.ubcmmhc_web.Repository;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Membership entity operations.
 */
@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    @Query("SELECT m FROM Membership m WHERE LOWER(m.email) = LOWER(:email)")
    Optional<Membership> findByEmail(String email);

    Optional<Membership> findByStripeCustomerId(String stripeCustomerId);

    Optional<Membership> findByStripeSubscriptionId(String subscriptionId);

    Optional<Membership> findByStripeSessionId(String sessionId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Membership m WHERE LOWER(m.email) = LOWER(:email)")
    boolean existsByEmail(String email);
}
