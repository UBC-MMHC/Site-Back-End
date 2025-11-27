package com.ubcmmhcsoftware.ubcmmhc_web.Repository;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Retrieves a user by ID and eagerly loads their associated roles.
     * <p>
     * This uses a {@code LEFT JOIN FETCH} to initialize the 'user_roles' collection
     * in a single query. This prevents {@code LazyInitializationException} when
     * accessing roles outside the transactional context (e.g., in the JWT Filter).
     * </p>
     *
     * @param id The UUID of the user.
     * @return The User entity with roles populated, or empty if not found.
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.user_roles WHERE u.id = :id")
    Optional<User> findUserByIdWithRoles(UUID id);

    /**
     * Retrieves a user by Email and eagerly loads their associated roles.
     * <p>
     * This uses a {@code LEFT JOIN FETCH} to initialize the 'user_roles' collection
     * in a single query. This prevents {@code LazyInitializationException} when
     * accessing roles outside the transactional context (e.g., in the JWT Filter).
     * </p>
     *
     * @param email The email of the user.
     * @return The User entity with roles populated, or empty if not found.
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.user_roles WHERE u.email = :email")
    Optional<User> findUserByEmail(String email);
}
