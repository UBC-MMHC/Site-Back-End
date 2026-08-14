package com.ubcmmhcsoftware.identity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserLookupImpl implements UserLookup {

    private final UserRepository userRepository;

    @Override
    public boolean exists(UUID userId) {
        return userId != null && userRepository.existsById(userId);
    }
}
