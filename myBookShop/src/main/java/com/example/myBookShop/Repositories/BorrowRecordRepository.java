package com.example.myBookShop.Repositories;

import com.example.myBookShop.Entity.borrowRecord;
import com.example.myBookShop.Enum.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface BorrowRecordRepository extends JpaRepository<borrowRecord, Integer> {

    boolean existsByUser_IdAndBook_IdAndStatusIn(int userId, int bookId, Collection<BorrowStatus> statuses);

    @Override
    @EntityGraph(attributePaths = {"user", "book"})
    Page<borrowRecord> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "book"})
    Page<borrowRecord> findByUser_Id(int userId, Pageable pageable);

    // Một câu UPDATE thay vì findAll rồi lọc trong Java: nhanh hơn và không đụng tới phiếu đã trả.
    @Modifying
    @Query("""
            update borrowRecord r
            set r.status = com.example.myBookShop.Enum.BorrowStatus.OVERDUE
            where r.status = com.example.myBookShop.Enum.BorrowStatus.BORROWING
              and r.returnDate < :today
            """)
    int markOverdue(@Param("today") LocalDate today);
}
