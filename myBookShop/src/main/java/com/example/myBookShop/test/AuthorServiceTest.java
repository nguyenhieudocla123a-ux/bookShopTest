package com.example.myBookShop.test;

import com.example.myBookShop.Entity.Author;
import com.example.myBookShop.Entity.Book;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Mapper.BookMapper;
import com.example.myBookShop.Repositories.AuthorRepository;
import com.example.myBookShop.Repositories.BookRepository;
import com.example.myBookShop.Service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.xml.validation.Validator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private BookMapper bookMapper;


    @Mock
    RedisTemplate<String, Object> redisTemplate;
    @Mock
    private  ObjectMapper objectMapper;

    static String booksKey(int authorId) {
        return "author:" + authorId + ":book";
    }
    @InjectMocks
    private AuthorService authorService;


    @Autowired
    private Validator validator;
    @Test
    public void delete_whenNotExcept() {
        //arrange
        int test_id = 1;
        String test_name = "test";
        Author author = new Author();
        author.setId(test_id);
        author.setName(test_name);
        author.setTheirBooks(List.of());
        when(authorRepository.findById(test_id)).thenReturn(Optional.of(author));
        authorService.delete(test_id);
        //check
        verify(authorRepository,times(1)).delete(any(Author.class));
    }

    @Test
    public void delete_whenAuthor_NotExist() {
        int test_id = 1;
        String test_name = "test";
        Author author = new Author();
        author.setId(test_id);
        author.setName(test_name);
        when(authorRepository.findById(test_id)).thenReturn(Optional.empty());
        //act
        //check
        assertThrows(ResourceNotFoundException.class,()-> authorService.delete(test_id));
        //
    }
    @Test
    public void delete_whenTheirBooks_Exist() {
        int test_id = 1;
        String test_name = "test";

        Author author = new Author();
        author.setId(test_id);
        author.setName(test_name);

        Book book = new Book();
        book.setId(test_id);
        author.setTheirBooks(List.of(book));
        book.setAuthor(author);
        when(authorRepository.findById(test_id)).thenReturn(Optional.of(author));

        assertThrows(ConflictDataException.class,()-> authorService.delete(test_id));

        verify(authorRepository,times(1)).findById(anyInt());
    }

}
