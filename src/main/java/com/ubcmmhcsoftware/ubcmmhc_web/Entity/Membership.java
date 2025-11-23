package com.ubcmmhcsoftware.ubcmmhc_web.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    private User user;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private boolean active;


    public Membership() {
        startDate = LocalDateTime.now();

        // TODO current we need to mannually cahnge this think of better way
        endDate = LocalDateTime.of(2026, 6, 1, 0, 0);
    }
}