package com.ubcmmhcsoftware.ubcmmhc_web.RepositoryTest;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTests {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    TestEntityManager testEntityManager;


    @Test
    public void findUserByIdWithRoles_Success() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("TestUser@test.com");
        user.setUser_roles(Set.of());

        User saved = testEntityManager.persistFlushFind(user);

        testEntityManager.clear();

        Optional<User> result = userRepository.findUserByIdWithRoles(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getUser_roles()).isEmpty();
    }

    @Test
    public void findUserByIdWithRoles_fetchesRoles() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("TestUser@test.com");

        RoleEnum roleEnum = RoleEnum.ROLE_USER;
        Role role = new Role();
        role.setName(roleEnum);

        user.setUser_roles(Set.of(role));

        testEntityManager.persist(user);
        testEntityManager.persist(role);
        testEntityManager.flush();
        testEntityManager.clear();

        Optional<User> result = userRepository.findUserByIdWithRoles(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUser_roles()).hasSize(1);
    }



    @Test
    public void finUserByEmail_Success() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("TestUser@test.com");
        user.setUser_roles(Set.of());

        User saved = testEntityManager.persistFlushFind(user);

        testEntityManager.clear();

        Optional<User> result = userRepository.findUserByEmail(saved.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getUser_roles()).isEmpty();
    }
}
