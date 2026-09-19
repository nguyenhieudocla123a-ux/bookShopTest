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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class BorrowRecordService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowMapper borrowMapper;
    @Transactional
    public BorrowRecordResponse create(CreateBorrowRecordRequest request){
        User user=userRepository.findById(request.getUserId()).orElseThrow(()->new ResourceNotFoundException("Not found User"));
        Book book =bookRepository.findById(request.getBookId()).orElseThrow(()-> new ResourceNotFoundException("Book Not Found "));
        //Check constant if userId and bookId are existed in table ,
        // they will reject , and then throw a Conflict Exception
        if(borrowRecordRepository.existsByUser_IdAndBook_Id(request.getUserId(),request.getBookId())) throw  new ConflictDataException("User have borrowed this book");
        borrowRecord newBorrow=new borrowRecord();
        //Update quantity of book - 1
        book.setQuantity(book.getQuantity()-1);
        newBorrow.setBook(book);
        newBorrow.setUser(user);
        newBorrow.setStatus(BorrowStatus.BORROWING.name());
        newBorrow.setBorrowDate(LocalDate.now());
        newBorrow.setReturnDate(LocalDate.now().plusDays(15));
        borrowRecordRepository.save(newBorrow);
        return borrowMapper.toResponse(newBorrow);
    }

    //Update overdue borrow
    @Scheduled(fixedRate = 5000)
    @Transactional
    public  void updateOverdue(){
     borrowRecordRepository.findAll().stream().filter(borrow->borrow.getReturnDate().isBefore(LocalDate.now())).forEach(borrow->borrow.setStatus(BorrowStatus.OVERDUE.name()));
    }

    //we wil delete borrows if state of book is returned

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public  void updateDelete(){
       List<borrowRecord> list= borrowRecordRepository.findAll().stream().filter(borrow->borrow.getStatus().equals(BorrowStatus.RETURNED.name())).toList();
       borrowRecordRepository.deleteAll(list);
    }


}
