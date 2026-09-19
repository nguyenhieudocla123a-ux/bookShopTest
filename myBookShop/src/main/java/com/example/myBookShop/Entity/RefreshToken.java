package com.example.myBookShop.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Table(name = "refresh_tokens")
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name ="token")
    private String token;

    @Column(name="expires_at")
    private Instant expiryAt;

    @Column(name ="revoked")
    private boolean revoked;

    @Column(name="created_at")
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User usersToken;
}
