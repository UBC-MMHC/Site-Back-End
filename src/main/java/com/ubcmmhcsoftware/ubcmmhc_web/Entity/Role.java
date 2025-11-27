package com.ubcmmhcsoftware.ubcmmhc_web.Entity;

import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a security role or authority within the system (e.g., ADMIN, MEMBER).
 * <p>
 * This entity is part of a Many-to-Many relationship with {@link User}.
 * decoupling roles into their own table allows for scalable permission management.
 * </p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true)
    private RoleEnum name;

    public Role(RoleEnum roleEnum) {
        this.name = roleEnum;
    }
}
