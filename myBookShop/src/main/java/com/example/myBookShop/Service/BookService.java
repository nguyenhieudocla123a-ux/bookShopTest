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
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

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
          Object object=redisTemplate.opsForHash().entries(key);
          if(object!=null){
              return bookMapper.toResponse( (Book) object);
          }
          //Save cache
          Book book= bookRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Book Not Found "));
          redisTemplate.opsForHash().putAll(key,objectMapper.convertValue(book, HashMap.class));
          return bookMapper.toResponse(book);

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
}
