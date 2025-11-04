package com.ubcmmhcsoftware.ubcmmhc_web.Repository;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}
