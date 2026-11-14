package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Request.CreateBookRequest;
import com.example.myBookShop.DTO.Request.UpdateBookRequest;
import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.DTO.Respone.BookViewResponse;
import com.example.myBookShop.Entity.Author;
import com.example.myBookShop.Entity.Book;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Mapper.BookMapper;
import com.example.myBookShop.Repositories.AuthorRepository;
import com.example.myBookShop.Repositories.BookRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService {
    private static final Duration BOOK_CACHE_TTL = Duration.ofMinutes(10);
    private static final Duration RECENT_TTL = Duration.ofMinutes(10);
    private static final Duration RANKING_TTL = Duration.ofDays(7);
    // Key mới vì ranking cũ lưu title (có thể trùng), ranking này lưu id sách.
    private static final String RANKING_KEY = "book:views:ranking";

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final AuthorRepository authorRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public BookResponse create(CreateBookRequest request) {
        Author author = authorRepository.findFirstByNameOrderByIdAsc(request.getAuthorName())
                .orElseThrow(() -> new ResourceNotFoundException("Not Found Author"));
        Book newBook = new Book();
        newBook.setTitle(request.getTitle());
        newBook.setAuthor(author);
        newBook.setQuantity(request.getQuantity());
        newBook.setPrice(request.getPrice());
        if(bookRepository.existsByTitle(request.getTitle())) {
            throw  new ConflictDataException("Title already exists");
        }
        bookRepository.save(newBook);
        redisTemplate.delete(AuthorService.booksKey(author.getId()));
        return bookMapper.toResponse(newBook);
    }

    /** Cache-aside: đọc cache trước, không có mới đọc DB rồi lưu lại kèm thời hạn. */
    @Transactional(readOnly = true)
    public BookResponse getBook(int id) {
        String key = bookKey(id);
        Map<Object, Object> cached = redisTemplate.opsForHash().entries(key);
        if (!cached.isEmpty()) {
            return objectMapper.convertValue(cached, BookResponse.class);
        }
        Book book = findBook(id);
        BookResponse response = bookMapper.toResponse(book);
        redisTemplate.opsForHash().putAll(key, objectMapper.convertValue(response, new TypeReference<HashMap<String, Object>>() {
        }));
        redisTemplate.expire(key, BOOK_CACHE_TTL);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(bookMapper::toResponse);
    }

    /** Dữ liệu đổi thì phải xoá cache, nếu không người dùng sẽ thấy dữ liệu cũ. */
    @Transactional
    public BookResponse update(int id, UpdateBookRequest request) {
        Book book = findBook(id);
        book.setPrice(request.getPrice());
        book.setQuantity(request.getQuantity());
        book.setTitle(request.getTitle());
        redisTemplate.delete(bookKey(id));
        redisTemplate.delete(AuthorService.booksKey(book.getAuthor().getId()));
        return bookMapper.toResponse(book);
    }

    public List<Object> getRecentBooks(int userId) {
        return redisTemplate.opsForList().range(recentKey(userId), 0, -1);
    }

    /** Xem sách: trả dữ liệu, lưu vào danh sách xem gần đây và tăng lượt xem. */
    @Transactional(readOnly = true)
    public BookResponse viewBook(int bookId, int userId) {
        Book book = findBook(bookId);
        recordRecentBook(userId, bookId);
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, bookId, 1);
        redisTemplate.expire(RANKING_KEY, RANKING_TTL);
        return bookMapper.toResponse(book);
    }

    public void setViews(int bookId, int views) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book Not Found!");
        }
        redisTemplate.opsForZSet().add(RANKING_KEY, bookId, views);
        redisTemplate.expire(RANKING_KEY, RANKING_TTL);
    }

    public List<BookViewResponse> topViews(int count) {
        Set<ZSetOperations.TypedTuple<Object>> ranking =
                redisTemplate.opsForZSet().reverseRangeWithScores(RANKING_KEY, 0, count - 1);
        if (ranking == null || ranking.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> ids = ranking.stream().map(tuple -> ((Number) tuple.getValue()).intValue()).toList();
        Map<Integer, String> titles = new HashMap<>();
        bookRepository.findAllById(ids).forEach(book -> titles.put(book.getId(), book.getTitle()));
        return ranking.stream()
                .filter(tuple -> titles.containsKey(((Number) tuple.getValue()).intValue()))
                .map(tuple -> {
                    int id = ((Number) tuple.getValue()).intValue();
                    return new BookViewResponse(id, titles.get(id), tuple.getScore() == null ? 0 : tuple.getScore().longValue());
                })
                .toList();
    }

    private void recordRecentBook(int userId, int bookId) {
        String key = recentKey(userId);
        // Xoá id trùng trước khi đẩy lên đầu danh sách, sau đó chỉ giữ 5 phần tử mới nhất.
        redisTemplate.opsForList().remove(key, 0, bookId);
        redisTemplate.opsForList().leftPush(key, bookId);
        redisTemplate.opsForList().trim(key, 0, 4);
        redisTemplate.expire(key, RECENT_TTL);
    }

    private Book findBook(int id) {
        return bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book Not Found!"));
    }

    private String bookKey(int id) {
        return "book:" + id;
    }

    private String recentKey(int userId) {
        return "recent:book:" + userId;
    }
}
