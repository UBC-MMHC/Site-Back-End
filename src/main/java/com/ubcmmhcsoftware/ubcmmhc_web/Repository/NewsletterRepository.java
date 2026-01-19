package com.ubcmmhcsoftware.ubcmmhc_web.Repository;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface NewsletterRepository extends JpaRepository<NewsletterSubscriber, UUID> {

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NewsletterSubscriber n WHERE LOWER(n.email) = LOWER(:email)")
    boolean existsByEmail(String email);

    @Query("SELECT n FROM NewsletterSubscriber n WHERE LOWER(n.email) = LOWER(:email)")
    NewsletterSubscriber findByEmail(String email);
}
