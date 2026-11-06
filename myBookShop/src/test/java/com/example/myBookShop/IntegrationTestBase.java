package com.example.myBookShop;

import com.example.myBookShop.Entity.Author;
import com.example.myBookShop.Entity.Book;
import com.example.myBookShop.Entity.Role;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Entity.userRole;
import com.example.myBookShop.Repositories.AuthorRepository;
import com.example.myBookShop.Repositories.BookRepository;
import com.example.myBookShop.Repositories.RoleRepository;
import com.example.myBookShop.Repositories.UserRepository;
import com.example.myBookShop.Repositories.UserRoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lớp nền cho test tích hợp: chạy đủ Controller, Security, Service, JPA nhưng thay Redis bằng mock.
 * Mỗi test tự tạo user và sách với tên ngẫu nhiên nên các test không ảnh hưởng lẫn nhau.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {
    protected static final String PASSWORD = "Password123";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected RoleRepository roleRepository;
    @Autowired protected UserRoleRepository userRoleRepository;
    @Autowired protected BookRepository bookRepository;
    @Autowired protected AuthorRepository authorRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    @MockitoBean protected RedisTemplate<String, Object> redisTemplate;

    protected ValueOperations<String, Object> valueOps;
    protected ListOperations<String, Object> listOps;
    protected HashOperations<String, Object, Object> hashOps;
    protected ZSetOperations<String, Object> zSetOps;

    /** Dữ liệu ghi qua opsForValue được giữ tại đây để luồng đăng ký chạy như với Redis thật. */
    protected final Map<String, Object> fakeValues = new ConcurrentHashMap<>();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUpRedisMock() {
        valueOps = mock(ValueOperations.class);
        listOps = mock(ListOperations.class);
        hashOps = mock(HashOperations.class);
        zSetOps = mock(ZSetOperations.class);
        fakeValues.clear();
        AtomicLong attempts = new AtomicLong();

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(redisTemplate.opsForHash()).thenAnswer(invocation -> hashOps);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        when(valueOps.get(anyString())).thenAnswer(call -> fakeValues.get(call.<String>getArgument(0)));
        org.mockito.Mockito.doAnswer(call -> {
            // Redis thật trả về JSON dạng Map, không phải object gốc.
            fakeValues.put(call.getArgument(0), objectMapper.convertValue(call.getArgument(1), HashMap.class));
            return null;
        }).when(valueOps).set(anyString(), any(), any(java.time.Duration.class));
        when(valueOps.increment(anyString())).thenAnswer(call -> attempts.incrementAndGet());
        when(redisTemplate.delete(anyString())).thenAnswer(call -> fakeValues.remove(call.<String>getArgument(0)) != null);

        when(hashOps.entries(anyString())).thenReturn(Map.of());
        when(listOps.range(anyString(), anyLong(), anyLong())).thenReturn(List.of());
    }

    protected User createUser(String roleName) {
        return createUser(roleName, true);
    }

    protected User createUser(String roleName, boolean enabled) {
        User user = new User();
        user.setUserName("user-" + UUID.randomUUID());
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setPassWord(passwordEncoder.encode(PASSWORD));
        user.setEnable(enabled);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        userRoleRepository.save(new userRole(user, role(roleName)));
        return user;
    }

    protected Role role(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            return roleRepository.save(role);
        });
    }

    protected Author createAuthor() {
        Author author = new Author();
        author.setName("author-" + UUID.randomUUID());
        author.setCountry("Vietnam");
        return authorRepository.save(author);
    }

    protected Book createBook(int quantity) {
        Book book = new Book();
        book.setTitle("book-" + UUID.randomUUID());
        book.setPrice(new BigDecimal("85000.00"));
        book.setQuantity(quantity);
        book.setAuthor(createAuthor());
        return bookRepository.save(book);
    }

    protected JsonNode login(String userName, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("userName", userName, "passWord", password));
        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data");
    }

    protected String bearer(User user) throws Exception {
        return "Bearer " + login(user.getUsername(), PASSWORD).get("accessToken").asText();
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
