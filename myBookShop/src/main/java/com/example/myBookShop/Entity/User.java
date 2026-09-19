package com.example.myBookShop.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int Id;

    @Column(name="username")
    String userName;

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",message = "Email not valid")
    @Column(name="email")
    String email;

    @Column(name = "password")
    String passWord;

    @Column(name = "phone")
    String phone;

    @Column(name="enabled")
    boolean enable;

    @Column(name="created_at")
    LocalDateTime createdAt;

    //To get all roles of user
    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER)
    List<userRole> userRoleList;

    //To get all borrow records of user

    @OneToMany(mappedBy ="user")
    List<borrowRecord> borrowRecordList;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.getUserRoleList().stream().map(item->new SimpleGrantedAuthority("ROLE_"+ item.getRole().getName().toUpperCase())).toList();
    }
    // Refresh token
    @OneToMany(mappedBy = "usersToken")
    List<RefreshToken>  refreshToken;
    @Override
    public String getPassword() {
        return this.passWord;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
