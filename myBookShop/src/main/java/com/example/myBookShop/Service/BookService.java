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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookService {
      private final BookRepository bookRepository;
      private final BookMapper bookMapper;
      private final AuthorRepository authorRepository;


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
