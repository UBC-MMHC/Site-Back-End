package com.ubcmmhcsoftware.auth.repository;

import com.ubcmmhcsoftware.auth.entity.User;
import com.ubcmmhcsoftware.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);

    void deleteByUser_Email(String userEmail);

    Optional<VerificationToken> findByUser(User user);
}
