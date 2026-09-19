package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository  extends JpaRepository<Role,Integer> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);
}
