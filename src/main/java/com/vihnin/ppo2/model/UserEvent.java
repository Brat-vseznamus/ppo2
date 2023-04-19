package com.vihnin.ppo2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class UserEvent {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long id;

    public LocalDateTime created;

    public Long userId;

    public Event type;

    public enum Event {
        ENTER, EXIT
    }
}
