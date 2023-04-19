package com.vihnin.ppo2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class GymUser {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long id;

    public String username;

    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime expiration;
}
