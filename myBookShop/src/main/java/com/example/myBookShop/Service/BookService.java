package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Request.CreateBookRequest;
import com.example.myBookShop.DTO.Request.UpdateBookRequest;
import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.Entity.Author;
import com.example.myBookShop.Entity.Book;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Mapper.BookMapper;
import com.example.myBookShop.Repositories.AuthorRepository;
import com.example.myBookShop.Repositories.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class BookService {
      private final BookRepository bookRepository;
      private final BookMapper bookMapper;
      private final AuthorRepository authorRepository;
      private final RedisTemplate<String ,Object> redisTemplate;
      private final ObjectMapper objectMapper;
      //Add book to ours library

      @Transactional
      public BookResponse create(CreateBookRequest request){
          Author author=authorRepository.findByName(request.getAuthorName()).orElseThrow(()-> new ResourceNotFoundException("Not Found Author"));
          Book newBook = new Book();
          newBook.setTitle(request.getTitle());
          newBook.setAuthor(author);
          newBook.setQuantity(request.getQuantity());
          newBook.setPrice(request.getPrice());
          bookRepository.save(newBook);
          return bookMapper.toResponse(newBook);
      }

      public BookResponse getBook(int id){
          String key="book:"+id;
          //Firstly, check cache
          Map<Object,Object> object=redisTemplate.opsForHash().entries(key);
          if(!object.isEmpty()){
              return bookMapper.toResponse( objectMapper.convertValue(object,Book.class));
          }
          //Save cache
          Book book= bookRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Book Not Found "));
          redisTemplate.opsForHash().putAll(key,objectMapper.convertValue(bookMapper.toResponse(book), HashMap.class));
          return bookMapper.toResponse(book);

      }
      // recordRecentBook

      public void recordRecentBook(int userId,Integer bookId){
          String key="recent:book:" + userId;
          //remove duplicated id before record lastest id
          redisTemplate.opsForList().remove(key,0,bookId);
          redisTemplate.opsForList().leftPush(key,bookId);
          redisTemplate.opsForList().trim(key,0,4);
          redisTemplate.expire(key,10, TimeUnit.MINUTES);
      }
      // Get all books

      public Page<BookResponse> getAll(Pageable pageable){
          return bookRepository.findAll(pageable).map(bookMapper::toResponse);
      }

      //update title ,price,quantity
      @Transactional
      public BookResponse update(UpdateBookRequest request) {
          Book book=bookRepository.findById(request.getId()).orElseThrow(()-> new ResourceNotFoundException("Book Not Found"));
          book.setPrice(request.getPrice());
          book.setQuantity(request.getQuantity());
          book.setTitle(request.getTitle());
          return bookMapper.toResponse(book);
      }

      public List<Object> getLatestSearch(int userId){
          String key="recent:book:" + userId;
          return redisTemplate.opsForList().range(key,0,-1);
      }
      public BookResponse searchBook(int bookId,int userId){
          Book book=bookRepository.findById(bookId).orElseThrow(()-> new ResourceNotFoundException("Book Not Found!"));
          recordRecentBook(userId,bookId);
          return bookMapper.toResponse(book);
      }


      //TODO view score book
      @Transactional
      public void bookView(int bookId, int view){
          Book book=bookRepository.findById(bookId).
                  orElseThrow(()-> new ResourceNotFoundException("Book Not Found!"));
          String key="book:ranking:views";
          redisTemplate.opsForZSet().add(key, book.getTitle(),view);
          redisTemplate.expire(key,7,TimeUnit.DAYS);
      }
     @Transactional
      public void increaseView(int bookId ){
         Book book=bookRepository.findById(bookId).
                 orElseThrow(()-> new ResourceNotFoundException("Book Not Found!"));
         String key="book:ranking:views";
         redisTemplate.opsForZSet().incrementScore(key,book.getTitle(),1);
      }

      //Top views ==> input count
      public Set<ZSetOperations.TypedTuple<Object>> topScore(int count){
          String key="book:ranking:views";
          return redisTemplate.opsForZSet().reverseRangeWithScores(key,0,count);
      }
}
