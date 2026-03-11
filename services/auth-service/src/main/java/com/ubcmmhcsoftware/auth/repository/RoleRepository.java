package com.ubcmmhcsoftware.auth.repository;

import com.ubcmmhcsoftware.auth.entity.Role;
import com.ubcmmhcsoftware.auth.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);

    boolean existsByName(RoleEnum name);
}
