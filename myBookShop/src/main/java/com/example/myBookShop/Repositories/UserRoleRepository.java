package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.Role;
import com.example.myBookShop.Entity.userRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<userRole,Integer> {
    Boolean existsByRole(Role role);
}
