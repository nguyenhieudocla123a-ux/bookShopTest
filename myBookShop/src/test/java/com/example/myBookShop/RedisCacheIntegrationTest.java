package com.example.myBookShop;

import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.Entity.Book;
import com.example.myBookShop.Service.AuthorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RedisCacheIntegrationTest extends IntegrationTestBase {
    @Autowired private AuthorService authorService;

    @Test
    @DisplayName("Lỗi cũ: rightPushAll đẩy cả danh sách thành một phần tử do chọn nhầm bản varargs")
    void authorBooksUseCollectionOverload() {
        Book book = createBook(1);
        int authorId = book.getAuthor().getId();

        List<BookResponse> result = authorService.findBook(authorId);

        assertThat(result).extracting(BookResponse::getId).containsExactly(book.getId());
        verify(listOps).rightPushAll(eq("author:" + authorId + ":book"), anyCollection());
        verify(listOps, never()).rightPushAll(any(), any(Object[].class));
        verify(redisTemplate).expire(eq("author:" + authorId + ":book"), any(Duration.class));
    }

    @Test
    void authorBooksAreReadFromCacheWhenPresent() {
        Book book = createBook(1);
        int authorId = book.getAuthor().getId();
        when(listOps.range("author:" + authorId + ":book", 0, -1)).thenReturn(List.of(
                Map.of("id", 777, "title", "cached", "price", new BigDecimal("85000.00"), "quantity", 1, "authorName", "x")));

        assertThat(authorService.findBook(authorId)).extracting(BookResponse::getTitle).containsExactly("cached");
    }

    @Test
    void getBookStoresCacheWithTtl() throws Exception {
        Book book = createBook(1);
        mockMvc.perform(get("/api/book/" + book.getId()).header("Authorization", bearer(createUser("USER"))))
                .andExpect(status().isOk());
        verify(hashOps).putAll(eq("book:" + book.getId()), any());
        verify(redisTemplate).expire(eq("book:" + book.getId()), any(Duration.class));
    }

    @Test
    @DisplayName("Lỗi cũ: cập nhật sách không xoá cache nên người dùng thấy dữ liệu cũ")
    void updateBookInvalidatesCache() throws Exception {
        Book book = createBook(1);
        mockMvc.perform(put("/api/book/" + book.getId()).header("Authorization", bearer(createUser("STAFF")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "new title", "price", 100, "quantity", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("new title"));

        verify(redisTemplate).delete("book:" + book.getId());
        verify(redisTemplate).delete("author:" + book.getAuthor().getId() + ":book");
    }

    @Test
    void viewingBookUsesCurrentUserAndIncreasesRanking() throws Exception {
        var user = createUser("USER");
        Book book = createBook(1);
        mockMvc.perform(get("/api/book/search/" + book.getId()).header("Authorization", bearer(user)))
                .andExpect(status().isOk());

        verify(listOps).leftPush("recent:book:" + user.getId(), book.getId());
        verify(zSetOps).incrementScore("book:views:ranking", book.getId(), 1);
    }

    @Test
    void topViewsReturnsBookIdsAndTitles() throws Exception {
        Book book = createBook(1);
        Set<ZSetOperations.TypedTuple<Object>> ranking = new LinkedHashSet<>();
        ranking.add(new DefaultTypedTuple<>((Object) book.getId(), 5.0));
        when(zSetOps.reverseRangeWithScores("book:views:ranking", 0, 2)).thenReturn(ranking);

        mockMvc.perform(get("/api/book/top-views/3").header("Authorization", bearer(createUser("USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].bookId").value(book.getId()))
                .andExpect(jsonPath("$.data[0].title").value(book.getTitle()))
                .andExpect(jsonPath("$.data[0].views").value(5));
    }
}
