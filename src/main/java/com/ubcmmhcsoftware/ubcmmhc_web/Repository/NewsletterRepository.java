package com.ubcmmhcsoftware.ubcmmhc_web.Repository;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NewsletterRepository extends JpaRepository<NewsletterSubscriber, UUID> {
    boolean existsByEmail(String email);
    NewsletterSubscriber findByEmail(String email);

}
