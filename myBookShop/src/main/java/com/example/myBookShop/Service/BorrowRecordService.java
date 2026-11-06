package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Request.CreateBorrowRecordRequest;
import com.example.myBookShop.DTO.Respone.BorrowRecordResponse;
import com.example.myBookShop.Entity.Book;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Entity.borrowRecord;
import com.example.myBookShop.Enum.BorrowStatus;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Mapper.BorrowMapper;
import com.example.myBookShop.Repositories.BookRepository;
import com.example.myBookShop.Repositories.BorrowRecordRepository;
import com.example.myBookShop.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BorrowRecordService {
    private static final List<BorrowStatus> ACTIVE_STATUSES = List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE);

    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowMapper borrowMapper;

    @Transactional
    public BorrowRecordResponse create(CreateBorrowRecordRequest request, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Not found User"));
        Book book = bookRepository.findById(request.getBookId()).orElseThrow(() -> new ResourceNotFoundException("Book Not Found"));

        if (borrowRecordRepository.existsByUser_IdAndBook_IdAndStatusIn(userId, book.getId(), ACTIVE_STATUSES)) {
            throw new ConflictDataException("You are already borrowing this book");
        }
        // Không đọc quantity rồi set lại: hai người cùng đọc được 1 cuốn sẽ đều mượn thành công.
        if (bookRepository.decreaseQuantity(book.getId()) == 0) {
            throw new ConflictDataException("Book Quantity Not Enough");
        }

        borrowRecord borrow = new borrowRecord();
        borrow.setBook(book);
        borrow.setUser(user);
        borrow.setStatus(BorrowStatus.BORROWING);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setReturnDate(LocalDate.now().plusDays(request.getBorrowDays()));
        borrowRecordRepository.save(borrow);
        return borrowMapper.toResponse(borrow);
    }

    /** Trả sách: chỉ chủ phiếu hoặc nhân viên được thao tác. Giữ phiếu lại làm lịch sử, không xoá. */
    @Transactional
    public BorrowRecordResponse returnBook(int borrowId, User currentUser) {
        borrowRecord borrow = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));

        if (borrow.getUser().getId() != currentUser.getId() && !isStaff(currentUser)) {
            throw new AccessDeniedException("You can only return your own books");
        }
        if (borrow.getStatus() == BorrowStatus.RETURNED) {
            throw new ConflictDataException("This book has already been returned");
        }

        borrow.setStatus(BorrowStatus.RETURNED);
        borrow.setReturnedDate(LocalDate.now());
        bookRepository.increaseQuantity(borrow.getBook().getId());
        return borrowMapper.toResponse(borrow);
    }

    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> getAll(Pageable pageable) {
        return borrowRecordRepository.findAll(pageable).map(borrowMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> getByUser(int userId, Pageable pageable) {
        return borrowRecordRepository.findByUser_Id(userId, pageable).map(borrowMapper::toResponse);
    }

    /** Hạn trả tính theo ngày nên quét mỗi giờ là đủ. fixedRate cũng chạy một lần ngay khi khởi động. */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    @Transactional
    public void updateOverdue() {
        borrowRecordRepository.markOverdue(LocalDate.now());
    }

    private boolean isStaff(User user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_STAFF"));
    }
}
