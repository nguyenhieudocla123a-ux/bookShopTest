package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RfTokenRepository extends JpaRepository<RefreshToken ,Integer> {

    void deleteAllByUsersToken_Id(int id);
    Optional<RefreshToken> findByToken(String token);
}
