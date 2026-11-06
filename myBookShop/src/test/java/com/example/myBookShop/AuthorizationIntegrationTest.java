package com.example.myBookShop;

import com.example.myBookShop.Entity.Author;
import com.example.myBookShop.Entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorizationIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("Lỗi cũ: USER tạo và xoá được tác giả")
    void userCannotCreateOrDeleteAuthor() throws Exception {
        String token = bearer(createUser("USER"));
        Author author = createAuthor();

        mockMvc.perform(post("/api/author").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "x", "country", "y"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(delete("/api/author/" + author.getId()).header("Authorization", token))
                .andExpect(status().isForbidden());
        assertThat(authorRepository.existsById(author.getId())).isTrue();
    }

    @Test
    void staffCanCreateAndDeleteAuthorWithoutBooks() throws Exception {
        String token = bearer(createUser("STAFF"));
        String response = mockMvc.perform(post("/api/author").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "author-" + UUID.randomUUID(), "country", "Vietnam"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = objectMapper.readTree(response).at("/data/id").asInt();

        mockMvc.perform(delete("/api/author/" + id).header("Authorization", token)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Lỗi cũ: USER không xem được sách")
    void userCanReadBooksButCannotChangeThem() throws Exception {
        String token = bearer(createUser("USER"));
        Book book = createBook(3);

        mockMvc.perform(get("/api/book").header("Authorization", token)).andExpect(status().isOk());
        mockMvc.perform(get("/api/book/" + book.getId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(book.getId()))
                .andExpect(jsonPath("$.data.authorName").value(book.getAuthor().getName()));

        mockMvc.perform(put("/api/book/" + book.getId()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "hacked", "price", 1, "quantity", 1))))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotAccessAdminData() throws Exception {
        String token = bearer(createUser("USER"));
        mockMvc.perform(get("/api/user").header("Authorization", token)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/role").header("Authorization", token)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/userRole").header("Authorization", token)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/borrow").header("Authorization", token)).andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadAdminDataWithoutLazyLoadingErrors() throws Exception {
        String token = bearer(createUser("ADMIN"));
        mockMvc.perform(get("/api/user").header("Authorization", token)).andExpect(status().isOk());
        mockMvc.perform(get("/api/role").header("Authorization", token)).andExpect(status().isOk());
        mockMvc.perform(get("/api/userRole").header("Authorization", token)).andExpect(status().isOk());
        mockMvc.perform(get("/api/borrow").header("Authorization", token)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Lỗi cũ: UserService.create luôn lỗi vì gọi get() trên Optional rỗng")
    void adminCanCreateUserAndNewUserCanLogin() throws Exception {
        String token = bearer(createUser("ADMIN"));
        String userName = "created-" + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/user").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userName", userName, "email", userName + "@example.com", "passWord", PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userName").value(userName))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());

        // Mật khẩu phải được mã hoá, nếu lưu thô thì login BCrypt sẽ thất bại.
        assertThat(login(userName, PASSWORD).get("accessToken").asText()).isNotBlank();
    }

    @Test
    @DisplayName("Lỗi cũ: @Valid không có tác dụng vì DTO không có ràng buộc")
    void invalidBookIsRejected() throws Exception {
        String token = bearer(createUser("STAFF"));
        mockMvc.perform(post("/api/book").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "", "price", -1, "quantity", -1, "authorName", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.price").exists());
    }

    @Test
    @DisplayName("Lỗi cũ: page mặc định là 1 nên bỏ qua trang đầu")
    void bookListStartsAtFirstPageAndRejectsInvalidPaging() throws Exception {
        String token = bearer(createUser("USER"));
        mockMvc.perform(get("/api/book").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.first").value(true));
        mockMvc.perform(get("/api/book?page=-1").header("Authorization", token)).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/book?size=100000").header("Authorization", token)).andExpect(status().isBadRequest());
    }

    @Test
    void missingBookReturns404AndBadIdReturns400() throws Exception {
        String token = bearer(createUser("USER"));
        mockMvc.perform(get("/api/book/999999").header("Authorization", token)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/book/abc").header("Authorization", token)).andExpect(status().isBadRequest());
    }
}
