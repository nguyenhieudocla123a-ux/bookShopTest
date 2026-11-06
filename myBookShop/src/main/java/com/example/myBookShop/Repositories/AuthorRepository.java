package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Integer> {

    // Tên tác giả có thể trùng, findByName sẽ ném lỗi khi có từ 2 kết quả.
    Optional<Author> findFirstByNameOrderByIdAsc(String name);
}
