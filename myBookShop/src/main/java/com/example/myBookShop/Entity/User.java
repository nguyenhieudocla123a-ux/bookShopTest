package com.example.myBookShop.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username")
    private String userName;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String passWord;

    @Column(name = "phone")
    private String phone;

    @Column(name = "enabled")
    private boolean enable;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // LAZY: danh sách user không cần role. Khi đăng nhập, UserRepository.findByUserName tải sẵn role.
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<userRole> userRoleList;

    @OneToMany(mappedBy = "user")
    private List<borrowRecord> borrowRecordList;

    @OneToMany(mappedBy = "usersToken")
    private List<RefreshToken> refreshToken;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.getUserRoleList().stream()
                .map(item -> new SimpleGrantedAuthority("ROLE_" + item.getRole().getName().toUpperCase()))
                .toList();
    }

    @Override
    public String getPassword() {
        return this.passWord;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    // Trước đây luôn trả true nên cột enabled không có tác dụng và tài khoản bị khoá vẫn đăng nhập được.
    @Override
    public boolean isEnabled() {
        return this.enable;
    }
}
