package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Integer> {

    // Tải author trong cùng một câu query để tránh lỗi N+1.
    @Override
    @EntityGraph(attributePaths = "author")
    Page<Book> findAll(Pageable pageable);

    /**
     * Kiểm tra và trừ số lượng trong MỘT câu UPDATE nên database đảm bảo an toàn khi nhiều người mượn cùng lúc.
     * Kết quả 0 nghĩa là sách đã hết.
     */
    @Modifying
    @Query("update Book b set b.quantity = b.quantity - 1 where b.id = :id and b.quantity > 0")
    int decreaseQuantity(@Param("id") int id);

    @Modifying
    @Query("update Book b set b.quantity = b.quantity + 1 where b.id = :id")
    int increaseQuantity(@Param("id") int id);

    boolean existsByTitle(String title);

    //lấy sách theo tác giả
    @Query("SELECT b from Book b where b.author.id=:id")
    Book findByAuthorId(@Param("id") int id);
}
