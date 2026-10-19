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
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor

public class BorrowRecordService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowMapper borrowMapper;
    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper objectMapper;
    @Transactional
    public BorrowRecordResponse create(CreateBorrowRecordRequest request){
        User user=userRepository.findById(request.getUserId()).orElseThrow(()->new ResourceNotFoundException("Not found User"));
        Book book =bookRepository.findById(request.getBookId()).orElseThrow(()-> new ResourceNotFoundException("Book Not Found "));
        //if book's quantity is less than 0 ==> reject
        if(book.getQuantity() ==0) throw new ConflictDataException("Book Quantity Not Enough");
        //Check constant if userId and bookId are existed in table ,
        borrowRecord newBorrow=new borrowRecord();


        book.setQuantity(book.getQuantity()-1);
        newBorrow.setBook(book);
        newBorrow.setUser(user);
        newBorrow.setStatus(BorrowStatus.BORROWING.name());
        newBorrow.setBorrowDate(LocalDate.now());
        newBorrow.setReturnDate(LocalDate.now().plusDays(request.getReturnDate()));
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
    @Transactional
    public BorrowRecordResponse createBorrow(CreateBorrowRecordRequest  request){
         User user=userRepository.findById(request.getUserId()).orElseThrow(()->new ResourceNotFoundException("Not found User"));
         Book book=bookRepository.findById(request.getBookId()).orElseThrow(()->new ResourceNotFoundException("Book Not Found "));
         borrowRecord borrow=new borrowRecord();
         borrow.setUser(user);
         borrow.setBook(book);
         borrow.setBorrowDate(LocalDate.now());
         borrow.setReturnDate(LocalDate.now().plusDays(request.getReturnDate()));
         borrow.setStatus(BorrowStatus.BORROWING.name());
         borrowRecordRepository.save(borrow);
         return borrowMapper.toResponse(borrow);
    }

    @Transactional
    public List<BorrowRecordResponse> getAllBorrow(){
        //Redis Checking
        String key ="record:borrow";
        List<BorrowRecordResponse> list =redisTemplate.opsForList().range(key, 0, -1).stream().map(t->objectMapper.convertValue(t,BorrowRecordResponse.class)).toList();

        if(!list.isEmpty()){
            return list;
        }
        List<BorrowRecordResponse> listBorrow=borrowRecordRepository.findAll().stream().map(borrowMapper::toResponse).toList();
        if(!listBorrow.isEmpty()){
            redisTemplate.opsForList().rightPushAll(key,listBorrow);
        }
        return listBorrow;
    }
}
