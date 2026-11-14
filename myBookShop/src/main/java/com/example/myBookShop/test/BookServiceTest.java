package com.example.myBookShop.test;


import com.example.myBookShop.DTO.Request.CreateBookRequest;
import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.Entity.Author;
import com.example.myBookShop.Entity.Book;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Mapper.BookMapper;
import com.example.myBookShop.Repositories.AuthorRepository;
import com.example.myBookShop.Repositories.BookRepository;
import com.example.myBookShop.Service.BookService;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookMapper bookMapper;

    @Mock
    RedisTemplate<String, Object> redisTemplate;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @InjectMocks
    private BookService bookService;

    @Autowired
    private Validator validator;


    @Test
    public void createBook_whenNoException_returnBook() {
        System.out.println("1.createBook_whenNoException_returnBook: ");
        //Arrange
        when(bookRepository.existsByTitle("String Boot")).thenReturn(false);
        CreateBookRequest  request = new CreateBookRequest();
        request.setTitle("String Boot");
        request.setAuthorName("Hieudeptrai");
        request.setPrice(BigDecimal.ONE);
        request.setQuantity(1);
        Author author = new Author();
        author.setName(request.getAuthorName());
        Book book = new Book();
        book.setTitle("String Boot");
        book.setAuthor(author);
        BookResponse response = new BookResponse();
        response.setTitle("String Boot");
        when(authorRepository.findFirstByNameOrderByIdAsc(any())).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(response);


        //act
        BookResponse bookResponse=bookService.create(request);
        assertEquals("String Boot", bookResponse.getTitle());
        verify(bookRepository).save(any(Book.class));
        verify(redisTemplate).delete(anyString());

    }

    @Test
    public void createBook_whenNotFound_Author_returnBook() {
        System.out.println("2.createBook_whenNotFound_returnBook: ");
        CreateBookRequest  request = new CreateBookRequest();
        request.setTitle("String Boot");
        request.setAuthorName("Hieudeptrai");
        request.setPrice(BigDecimal.ONE);
        request.setQuantity(1);
        when(authorRepository.findFirstByNameOrderByIdAsc(any())).thenReturn(Optional.empty());
        //act
        assertThrows(ResourceNotFoundException.class, () -> bookService.create(request));
        verify(authorRepository,times(1)).findFirstByNameOrderByIdAsc(anyString());
    }

    @Test
    public void createBook_whenNotFound_Book_returnBook() {
        System.out.println("3.createBook_whenNotFound_returnBook: ");
        CreateBookRequest  request = new CreateBookRequest();
        request.setTitle("String Boot");
        request.setAuthorName("Hieudeptrai");
        request.setPrice(BigDecimal.ONE);
        request.setQuantity(1);
        Author author = new Author();
        author.setName(request.getAuthorName());
        Book book = new Book();
        book.setTitle("String Boot");
        book.setAuthor(author);
        when(authorRepository.findFirstByNameOrderByIdAsc(any())).thenReturn(Optional.of(author));
        when(bookRepository.existsByTitle(book.getTitle())).thenReturn(true);

        //act

        assertThrows(ConflictDataException.class,()->bookService.create(request));
        verify(authorRepository,times(1)).findFirstByNameOrderByIdAsc(anyString());
        verify(bookRepository,times(1)).existsByTitle(book.getTitle());
        verify(bookRepository,times(0)).save(any(Book.class));
    }
    @Test
    public void createBook_whenNotExact_Argument_returnBook() {
        System.out.println("4.createBook_whenNotExact_Argument_returnBook: ");
        CreateBookRequest  request = new CreateBookRequest();
        request.setTitle("String Boot");
        request.setAuthorName("Hieudeptrai");
        request.setPrice(BigDecimal.ONE.multiply(new BigDecimal(-1)));
        request.setQuantity(1);

        Set<ConstraintViolation<CreateBookRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v-> v.getPropertyPath().toString().equals("price")));

    }


}
