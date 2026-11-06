package com.example.myBookShop;

import com.example.myBookShop.DTO.Request.CreateBorrowRecordRequest;
import com.example.myBookShop.Entity.Book;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Entity.borrowRecord;
import com.example.myBookShop.Enum.BorrowStatus;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Repositories.BorrowRecordRepository;
import com.example.myBookShop.Service.BorrowRecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BorrowFlowIntegrationTest extends IntegrationTestBase {
    @Autowired private BorrowRecordService borrowRecordService;
    @Autowired private BorrowRecordRepository borrowRecordRepository;

    private int quantityOf(Book book) {
        return bookRepository.findById(book.getId()).orElseThrow().getQuantity();
    }

    private int borrow(String token, Book book) throws Exception {
        String response = mockMvc.perform(post("/api/borrow").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("bookId", book.getId(), "borrowDays", 7))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("BORROWING"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).at("/data/id").asInt();
    }

    @Test
    @DisplayName("Lỗi cũ: không có chức năng trả sách nên số lượng không được cộng lại")
    void borrowAndReturnUpdatesQuantityAndKeepsHistory() throws Exception {
        String token = bearer(createUser("USER"));
        Book book = createBook(2);

        int borrowId = borrow(token, book);
        assertThat(quantityOf(book)).isEqualTo(1);

        mockMvc.perform(post("/api/borrow/" + borrowId + "/return").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RETURNED"))
                .andExpect(jsonPath("$.data.returnedDate").value(LocalDate.now().toString()));
        assertThat(quantityOf(book)).isEqualTo(2);
        assertThat(borrowRecordRepository.existsById(borrowId)).isTrue();

        // Trả lần hai không được cộng thêm số lượng.
        mockMvc.perform(post("/api/borrow/" + borrowId + "/return").header("Authorization", token))
                .andExpect(status().isConflict());
        assertThat(quantityOf(book)).isEqualTo(2);
    }

    @Test
    void cannotBorrowWhenOutOfStock() throws Exception {
        String token = bearer(createUser("USER"));
        Book book = createBook(0);
        mockMvc.perform(post("/api/borrow").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("bookId", book.getId(), "borrowDays", 7))))
                .andExpect(status().isConflict());
    }

    @Test
    void cannotBorrowSameBookTwiceWhileActive() throws Exception {
        String token = bearer(createUser("USER"));
        Book book = createBook(5);
        borrow(token, book);
        mockMvc.perform(post("/api/borrow").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("bookId", book.getId(), "borrowDays", 7))))
                .andExpect(status().isConflict());
        assertThat(quantityOf(book)).isEqualTo(4);
    }

    @Test
    void invalidBorrowDaysAreRejected() throws Exception {
        String token = bearer(createUser("USER"));
        Book book = createBook(1);
        mockMvc.perform(post("/api/borrow").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("bookId", book.getId(), "borrowDays", 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Lỗi cũ: mượn sách theo userId gửi lên, có thể mượn thay người khác")
    void borrowerComesFromTokenNotFromRequest() throws Exception {
        User owner = createUser("USER");
        User other = createUser("USER");
        Book book = createBook(1);

        mockMvc.perform(post("/api/borrow").header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", other.getId(), "bookId", book.getId(), "borrowDays", 7))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userName").value(owner.getUsername()));
    }

    @Test
    void onlyOwnerOrStaffCanReturn() throws Exception {
        User owner = createUser("USER");
        Book book = createBook(1);
        int borrowId = borrow(bearer(owner), book);

        mockMvc.perform(post("/api/borrow/" + borrowId + "/return").header("Authorization", bearer(createUser("USER"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/borrow/" + borrowId + "/return").header("Authorization", bearer(createUser("STAFF"))))
                .andExpect(status().isOk());
    }

    @Test
    void myBorrowsOnlyShowMyRecords() throws Exception {
        User me = createUser("USER");
        String myToken = bearer(me);
        Book book = createBook(5);
        borrow(myToken, book);
        borrow(bearer(createUser("USER")), book);

        mockMvc.perform(get("/api/borrow/me").header("Authorization", myToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].userName").value(me.getUsername()));
    }

    @Test
    @DisplayName("Lỗi cũ: job định kỳ ghi đè OVERDUE lên cả phiếu đã trả")
    void overdueJobOnlyTouchesActiveRecords() {
        User user = createUser("USER");
        borrowRecord late = saveRecord(user, createBook(1), BorrowStatus.BORROWING, LocalDate.now().minusDays(1));
        borrowRecord returned = saveRecord(user, createBook(1), BorrowStatus.RETURNED, LocalDate.now().minusDays(1));
        borrowRecord onTime = saveRecord(user, createBook(1), BorrowStatus.BORROWING, LocalDate.now().plusDays(1));

        borrowRecordService.updateOverdue();

        assertThat(statusOf(late)).isEqualTo(BorrowStatus.OVERDUE);
        assertThat(statusOf(returned)).isEqualTo(BorrowStatus.RETURNED);
        assertThat(statusOf(onTime)).isEqualTo(BorrowStatus.BORROWING);
    }

    @Test
    @DisplayName("Lỗi cũ: nhiều người mượn cùng lúc có thể cho mượn quá số lượng")
    void concurrentBorrowsNeverExceedStock() throws Exception {
        Book book = createBook(2);
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(createUser("USER"));
        }

        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (User user : users) {
                tasks.add(() -> {
                    try {
                        borrowRecordService.create(new CreateBorrowRecordRequest(book.getId(), 7), user.getId());
                        return true;
                    } catch (ConflictDataException e) {
                        return false;
                    }
                });
            }
            int succeeded = 0;
            for (Future<Boolean> result : executor.invokeAll(tasks)) {
                if (result.get()) succeeded++;
            }
            assertThat(succeeded).isEqualTo(2);
            assertThat(quantityOf(book)).isZero();
        } finally {
            executor.shutdownNow();
        }
    }

    private borrowRecord saveRecord(User user, Book book, BorrowStatus status, LocalDate dueDate) {
        borrowRecord record = new borrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setStatus(status);
        record.setBorrowDate(LocalDate.now().minusDays(10));
        record.setReturnDate(dueDate);
        return borrowRecordRepository.save(record);
    }

    private BorrowStatus statusOf(borrowRecord record) {
        return borrowRecordRepository.findById(record.getId()).orElseThrow().getStatus();
    }
}
