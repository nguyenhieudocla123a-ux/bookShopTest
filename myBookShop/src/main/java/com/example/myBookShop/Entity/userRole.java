package com.example.myBookShop.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@IdClass(userRole.UserRoleId.class)
@Table(name = "user_roles")
public class userRole {

    @Id
    @JoinColumn(name="user_id")
    @ManyToOne
    User user;

    @Id
    @JoinColumn(name="role_id")
    @ManyToOne
    Role role;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleId implements Serializable {
        private int user;
        private int role;
    }
}
