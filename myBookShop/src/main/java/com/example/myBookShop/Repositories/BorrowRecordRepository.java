package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.borrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowRecordRepository extends JpaRepository<borrowRecord,Integer> {
    boolean existsByUser_IdAndBook_Id(int userId,int bookId);
}
