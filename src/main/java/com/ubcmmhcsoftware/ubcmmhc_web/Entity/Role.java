package com.ubcmmhcsoftware.ubcmmhc_web.Entity;

import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleEnum name;

    public Role(RoleEnum roleEnum) {
        this.name = roleEnum;
    }

    public Role() {}
}
