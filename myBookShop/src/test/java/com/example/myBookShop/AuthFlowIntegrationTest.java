package com.example.myBookShop;

import com.example.myBookShop.Entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowIntegrationTest extends IntegrationTestBase {

    private ResultActions postJson(String url, Object body) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json(body)));
    }

    @Test
    @DisplayName("Lỗi cũ: sai mật khẩu vẫn đăng nhập được")
    void loginWithWrongPasswordIsRejected() throws Exception {
        User user = createUser("USER");
        postJson("/api/auth/login", Map.of("userName", user.getUsername(), "passWord", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginWithCorrectPasswordReturnsTokens() throws Exception {
        JsonNode tokens = login(createUser("USER").getUsername(), PASSWORD);
        assertThat(tokens.get("accessToken").asText()).isNotBlank();
        assertThat(tokens.get("refreshToken").asText()).isNotBlank();
    }

    @Test
    void unknownUserGetsTheSameErrorAsWrongPassword() throws Exception {
        postJson("/api/auth/login", Map.of("userName", "nobody-" + UUID.randomUUID(), "passWord", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.messages").value("Username or password is incorrect"));
    }

    @Test
    @DisplayName("Lỗi cũ: isEnabled luôn true nên tài khoản bị khoá vẫn đăng nhập được")
    void disabledUserCannotLogin() throws Exception {
        User user = createUser("USER", false);
        postJson("/api/auth/login", Map.of("userName", user.getUsername(), "passWord", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.messages").value("Account is disabled"));
    }

    @Test
    @DisplayName("Lỗi cũ: ai cũng tạo được refresh token cho userId bất kỳ")
    void publicRefreshTokenEndpointIsGone() throws Exception {
        postJson("/api/refresh-token/1", Map.of()).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRotatesTokenAndOldTokenCannotBeReused() throws Exception {
        String oldToken = login(createUser("USER").getUsername(), PASSWORD).get("refreshToken").asText();

        postJson("/api/auth/refresh", Map.of("refreshToken", oldToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());

        postJson("/api/auth/refresh", Map.of("refreshToken", oldToken)).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutInvalidatesRefreshToken() throws Exception {
        String token = login(createUser("USER").getUsername(), PASSWORD).get("refreshToken").asText();

        postJson("/api/auth/logout", Map.of("refreshToken", token)).andExpect(status().isOk());
        postJson("/api/auth/refresh", Map.of("refreshToken", token)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Lỗi cũ: token hỏng làm filter ném exception thay vì trả 401")
    void malformedJwtReturns401() throws Exception {
        mockMvc.perform(get("/api/book").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void requestWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/book")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Lỗi cũ: đăng ký treo vì chờ nhập mã từ console server")
    void registerThenVerifyCreatesUserWhoCanLogin() throws Exception {
        String userName = "new-" + UUID.randomUUID().toString().substring(0, 8);
        postJson("/api/auth/register", Map.of("userName", userName, "passWord", PASSWORD, "email", userName + "@example.com"))
                .andExpect(status().isAccepted());

        assertThat(userRepository.findByUserName(userName)).isEmpty();
        String code = (String) ((Map<?, ?>) fakeValues.get("register:pending:" + userName)).get("code");

        postJson("/api/auth/register/verify", Map.of("userName", userName, "code", code))
                .andExpect(status().isCreated());

        assertThat(login(userName, PASSWORD).get("accessToken").asText()).isNotBlank();
    }

    @Test
    void wrongCodeDoesNotCreateUserAndTooManyAttemptsCancelRegistration() throws Exception {
        String userName = "new-" + UUID.randomUUID().toString().substring(0, 8);
        postJson("/api/auth/register", Map.of("userName", userName, "passWord", PASSWORD, "email", userName + "@example.com"))
                .andExpect(status().isAccepted());

        for (int i = 0; i < 5; i++) {
            postJson("/api/auth/register/verify", Map.of("userName", userName, "code", "wrong"))
                    .andExpect(status().isBadRequest());
        }
        assertThat(userRepository.findByUserName(userName)).isEmpty();
        assertThat(fakeValues).doesNotContainKey("register:pending:" + userName);
    }

    @Test
    void registerRejectsInvalidData() throws Exception {
        postJson("/api/auth/register", Map.of("userName", "ab", "passWord", "short", "email", "not-an-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.passWord").exists());
    }

    @Test
    void registerRejectsExistingUsername() throws Exception {
        User user = createUser("USER");
        postJson("/api/auth/register", Map.of("userName", user.getUsername(), "passWord", PASSWORD, "email", "a@example.com"))
                .andExpect(status().isConflict());
    }
}
