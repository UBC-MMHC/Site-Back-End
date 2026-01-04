package com.ubcmmhcsoftware.ubcmmhc_web.RepositoryTest;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.VerificationToken;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class VerificationTokenRepositoryTest {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    public void findByToken_Success() {
        User user = new User();
        user.setEmail("HEHE@gmail.com");
        user.setName("HEHE");
        testEntityManager.persist(user);
        testEntityManager.flush();

        VerificationToken token = new VerificationToken();
        token.setToken("token");
        token.setUser(user);

        VerificationToken saved = testEntityManager.persist(token);
        testEntityManager.flush();

        Optional<VerificationToken> result = verificationTokenRepository.findByToken("token");

        assertThat(result)
                .isPresent()
                .contains(saved);
    }

    @Test
    public void deleteByUser_EmailTest() {
        User user = new User();
        user.setEmail("HEHE@gmail.com");
        user.setName("HEHE");
        testEntityManager.persist(user);
        testEntityManager.flush();

        VerificationToken token = new VerificationToken();
        token.setToken("token");
        token.setUser(user);

        testEntityManager.persist(token);
        testEntityManager.flush();

        verificationTokenRepository.deleteByUser_Email(user.getEmail());
        testEntityManager.flush();

        Optional<VerificationToken> result = verificationTokenRepository.findByToken("token");

        assertThat(result)
                .isNotPresent();
    }

    @Test
    public void findByUser_Success() {
        User user = new User();
        user.setEmail("findbyuser@gmail.com");
        user.setName("FindByUser");
        testEntityManager.persist(user);
        testEntityManager.flush();

        VerificationToken token = new VerificationToken();
        token.setToken("userToken");
        token.setUser(user);

        testEntityManager.persist(token);
        testEntityManager.flush();

        Optional<VerificationToken> result = verificationTokenRepository.findByUser(user);

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(t -> assertThat(t.getToken()).isEqualTo("userToken"));
    }

    @Test
    public void findByUser_NotFound() {
        User user = new User();
        user.setEmail("notoken@gmail.com");
        user.setName("NoToken");
        testEntityManager.persist(user);
        testEntityManager.flush();

        Optional<VerificationToken> result = verificationTokenRepository.findByUser(user);

        assertThat(result).isEmpty();
    }

}