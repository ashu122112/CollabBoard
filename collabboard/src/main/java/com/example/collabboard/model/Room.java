package com.example.collabboard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Data // Lombok annotation to generate getters, setters, etc.
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomCode;

    // We can link this to the user who created the room
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // This method will be called before the room is saved to the database
    @PrePersist
    protected void onCreate() {
        // Generate a random, unique code for the room
        if (roomCode == null) {
            roomCode = UUID.randomUUID().toString().substring(0, 8);
        }
    }
}