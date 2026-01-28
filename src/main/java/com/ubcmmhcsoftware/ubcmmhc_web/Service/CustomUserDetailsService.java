package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Core Security Service that acts as a bridge between the Database and Spring
 * Security.
 * <p>
 * This class implements the standard Spring Security interface
 * {@link UserDetailsService}.
 * Its primary job is to load user data (like passwords and roles) from the
 * database
 * so the AuthenticationManager can compare it against the user's login input.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Locates the user based on the username (which is the Email in this
     * application).
     * <p>
     * This method is called automatically by Spring Security during the login
     * process.
     * It fetches the {@link User} entity and wraps it in a
     * {@link CustomUserDetails} object,
     * which contains the authorities/roles required for access control.
     * </p>
     *
     * @param email The email (username) identifying the user whose data is
     *              required.
     * @return A fully populated user record (CustomUserDetails).
     * @throws UsernameNotFoundException if the user cannot be found in the
     *                                   database.
     */
    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByEmail(email.toLowerCase());

        if (user.isPresent()) {
            return new CustomUserDetails(user.get());
        } else {
            throw new UsernameNotFoundException(email);
        }
    }

    /**
     * Loads a user by their unique UUID.
     * <p>
     * This method is typically used by the JWT Filter. When a request comes in with
     * a token,
     * we extract the User ID from the token and call this method to verify the user
     * still exists and to load their current roles/permissions.
     * </p>
     *
     * @param id The UUID of the user.
     * @return A fully populated user record (CustomUserDetails).
     * @throws UsernameNotFoundException if the user ID does not exist.
     */
    public CustomUserDetails loadUserById(UUID id) {
        Optional<User> user = userRepository.findUserByIdWithRoles(id);

        if (user.isPresent()) {
            return new CustomUserDetails(user.get());
        }
        throw new UsernameNotFoundException(id.toString());
    }
}
