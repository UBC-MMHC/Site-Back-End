package com.ubcmmhcsoftware.ubcmmhc_web.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    @Column(unique = true)
    private String googleId;
    @Column(unique = true)
    private String email;
    private String name;

    private String instagram;
    private Boolean instaChat;

    private String studentId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "users_id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id"))
    private Set<Role> user_roles = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    private VerificationToken verificationToken;

    private boolean newsletterSubscription = false; // False by default

    // TODO Future membership implementation
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Membership membership;

    public User(String email) {
        this.email = email;
    }
}