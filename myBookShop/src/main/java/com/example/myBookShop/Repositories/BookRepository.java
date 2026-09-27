package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BookRepository extends JpaRepository<Book,Integer> {
    @Query("""
            Select p
            From Book p
            where p.author.name= :name
            """)
    List<Book> findAllByAuthor_Name(@Param("name")String name);




}
