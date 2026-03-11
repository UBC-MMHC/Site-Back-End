package com.ubcmmhcsoftware.user.repository;

import com.ubcmmhcsoftware.user.entity.Role;
import com.ubcmmhcsoftware.user.entity.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}
