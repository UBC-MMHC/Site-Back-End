package com.ubcmmhcsoftware.user.repository;

import com.ubcmmhcsoftware.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.user_roles WHERE u.id = :id")
    Optional<User> findUserByIdWithRoles(UUID id);
}
