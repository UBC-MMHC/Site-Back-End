package com.ubcmmhcsoftware.ubcmmhc_web.RepositoryTest;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void findByName_Success() {
        Role role = new Role();
        role.setName(RoleEnum.ROLE_USER);
        testEntityManager.persist(role);
        testEntityManager.flush();

        Optional<Role> result = roleRepository.findByName(RoleEnum.ROLE_USER);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(RoleEnum.ROLE_USER);
    }

    @Test
    void findByName_NotFound() {
        Optional<Role> result = roleRepository.findByName(RoleEnum.ROLE_SUPERADMIN);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByName_ReturnsTrue() {
        Role role = new Role();
        role.setName(RoleEnum.ROLE_ADMIN);
        testEntityManager.persist(role);
        testEntityManager.flush();

        boolean exists = roleRepository.existsByName(RoleEnum.ROLE_ADMIN);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_ReturnsFalse() {
        boolean exists = roleRepository.existsByName(RoleEnum.ROLE_SUPERADMIN);

        assertThat(exists).isFalse();
    }
}
