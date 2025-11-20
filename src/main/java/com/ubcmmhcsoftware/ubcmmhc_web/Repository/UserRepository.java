package com.ubcmmhcsoftware.ubcmmhc_web.Repository;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // When we find User we eagerly load roles alongside
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.user_roles WHERE u.id = :id")
    Optional<User> findUserByIdWithRoles(UUID id);
    Optional<User> findUserByEmail(String email);

    User getUserByEmail(String email);
}
