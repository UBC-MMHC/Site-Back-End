package com.ubcmmhcsoftware.newsletter.repository;

import com.ubcmmhcsoftware.newsletter.entity.NewsletterSubscriber;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterRepository extends JpaRepository<NewsletterSubscriber, UUID> {

    Optional<NewsletterSubscriber> findByEmail(String email);
}
