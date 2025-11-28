 package com.ubcmmhcsoftware.ubcmmhc_web.Entity;

 import jakarta.persistence.*;
 import lombok.AllArgsConstructor;
 import lombok.Builder;
 import lombok.Data;
 import lombok.NoArgsConstructor;
 import org.springframework.data.annotation.CreatedDate;

 import java.time.LocalDateTime;
 import java.util.UUID;

 @Entity
 @Data
 @NoArgsConstructor
 @AllArgsConstructor
 @Builder
 @Table(name = "newsletter_subscriber")
 public class NewsletterSubscriber {
     @Id
     @GeneratedValue(strategy = GenerationType.UUID)
     private UUID id;
     @Column(unique = true)
     private String email;
     @CreatedDate
     private LocalDateTime createdAt;
     private boolean unsubscribed;
     private LocalDateTime unsubscribedTime;

     @OneToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id")
     private User user;
 }
