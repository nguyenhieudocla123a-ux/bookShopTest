package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.Role;
import com.example.myBookShop.Entity.userRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<userRole, userRole.UserRoleId> {
    Boolean existsByRole(Role role);

    @Override
    @EntityGraph(attributePaths = {"user", "role"})
    List<userRole> findAll();
}
