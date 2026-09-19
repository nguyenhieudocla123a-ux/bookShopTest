package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.Author;
import com.example.myBookShop.Entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author,Integer> {

    Optional<Author> findByName(String name);
}
