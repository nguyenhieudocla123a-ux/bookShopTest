package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Đăng nhập và JWT filter đều cần role để tạo authorities nên tải sẵn tại đây.
    @EntityGraph(attributePaths = {"userRoleList", "userRoleList.role"})
    Optional<User> findByUserName(String username);

    boolean existsByEmail(String email);
}
