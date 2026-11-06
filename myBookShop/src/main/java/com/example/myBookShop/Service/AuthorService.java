package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Request.CreateAuthorRequest;
import com.example.myBookShop.DTO.Respone.AuthorResponse;
import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.Entity.Author;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Mapper.AuthorMapper;
import com.example.myBookShop.Mapper.BookMapper;
import com.example.myBookShop.Repositories.AuthorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorService {
    private static final Duration BOOKS_CACHE_TTL = Duration.ofMinutes(10);

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final BookMapper bookMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    static String booksKey(int authorId) {
        return "author:" + authorId + ":book";
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> getAll() {
        return authorRepository.findAll().stream().map(authorMapper::toResponse).toList();
    }

    @Transactional
    public AuthorResponse create(CreateAuthorRequest createAuthorRequest) {
        Author createdAuthor = authorMapper.fromCreateRequestToEntity(createAuthorRequest);
        authorRepository.save(createdAuthor);
        return authorMapper.toResponse(createdAuthor);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findBook(int id) {
        String key = booksKey(id);
        List<Object> cached = redisTemplate.opsForList().range(key, 0, -1);
        if (cached != null && !cached.isEmpty()) {
            return cached.stream().map(item -> objectMapper.convertValue(item, BookResponse.class)).toList();
        }

        Author author = findAuthor(id);
        List<BookResponse> books = author.getTheirBooks().stream().map(bookMapper::toResponse).toList();
        if (!books.isEmpty()) {
            // rightPushAll có 2 bản: (K, V...) và (K, Collection<V>). Với V = Object, List<BookResponse>
            // KHÔNG phải Collection<Object> (generics không hiệp biến), nên Java chọn bản varargs và đẩy
            // CẢ danh sách thành MỘT phần tử. Đổi sang List<Object> để gọi đúng bản Collection.
            List<Object> values = new ArrayList<>(books);
            redisTemplate.opsForList().rightPushAll(key, values);
            redisTemplate.expire(key, BOOKS_CACHE_TTL);
        }
        return books;
    }

    /** Chỉ xoá được tác giả khi không còn sách. */
    @Transactional
    public void delete(int id) {
        Author author = findAuthor(id);
        if (!author.getTheirBooks().isEmpty()) {
            throw new ConflictDataException("all book of this author still exist");
        }
        authorRepository.delete(author);
        redisTemplate.delete(booksKey(id));
    }

    private Author findAuthor(int id) {
        return authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found author"));
    }
}
