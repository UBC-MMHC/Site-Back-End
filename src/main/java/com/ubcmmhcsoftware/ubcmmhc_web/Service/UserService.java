package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for managing user-specific data and profile operations.
 * <p>
 * Distinction:
 * - {@link AuthService}: Handles "Entry" logic (Login, Register, Password Reset).
 * - {@link UserService}: Handles "Post-Entry" logic (Get Profile, Update Bio, Change Settings).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserService {
    /* * TODO: Potential methods to implement here:
     * * 1. Get User Profile:
     * public UserDTO getUserProfile(UUID userId) { ... }
     *
     * 2. Update User Details:
     * public void updateUser(UUID userId, UpdateUserDTO dto) { ... }
     *
     * 3. Change Password (Authenticated):
     * public void changePassword(String email, String oldPass, String newPass) { ... }
     */
}
