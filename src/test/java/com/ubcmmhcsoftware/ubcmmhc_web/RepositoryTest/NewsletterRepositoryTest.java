package com.ubcmmhcsoftware.ubcmmhc_web.RepositoryTest;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.NewsletterSubscriber;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.NewsletterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class NewsletterRepositoryTest {

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void findByEmail_Success() {
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .unsubscribed(false)
                .build();
        testEntityManager.persist(subscriber);
        testEntityManager.flush();

        NewsletterSubscriber result = newsletterRepository.findByEmail("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.isUnsubscribed()).isFalse();
    }

    @Test
    void findByEmail_NotFound() {
        NewsletterSubscriber result = newsletterRepository.findByEmail("nonexistent@example.com");

        assertThat(result).isNull();
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email("exists@example.com")
                .createdAt(LocalDateTime.now())
                .unsubscribed(false)
                .build();
        testEntityManager.persist(subscriber);
        testEntityManager.flush();

        boolean exists = newsletterRepository.existsByEmail("exists@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ReturnsFalse() {
        boolean exists = newsletterRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void findByEmail_ReturnsUnsubscribedSubscriber() {
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email("unsubscribed@example.com")
                .createdAt(LocalDateTime.now())
                .unsubscribed(true)
                .unsubscribedTime(LocalDateTime.now())
                .build();
        testEntityManager.persist(subscriber);
        testEntityManager.flush();

        NewsletterSubscriber result = newsletterRepository.findByEmail("unsubscribed@example.com");

        assertThat(result).isNotNull();
        assertThat(result.isUnsubscribed()).isTrue();
        assertThat(result.getUnsubscribedTime()).isNotNull();
    }
}
