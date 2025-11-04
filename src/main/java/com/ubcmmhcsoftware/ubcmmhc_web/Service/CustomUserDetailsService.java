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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByEmail(username);

        if (user.isPresent()) {
            return new CustomUserDetails(user.get());
        } else {
            throw new UsernameNotFoundException(username);
        }
    }

    public CustomUserDetails loadUserById(UUID id) {
        Optional<User> user = userRepository.findUserById(id);

        if (user.isPresent()) {
            return new CustomUserDetails(user.get());
        }
            throw new UsernameNotFoundException(id.toString());
        }
}
