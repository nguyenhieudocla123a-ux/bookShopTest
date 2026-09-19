package com.example.myBookShop.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="borrow_records")
public class borrowRecord {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int Id;

    @ManyToOne
    @JoinColumn(name="user_id")
    User user;


    @ManyToOne
    @JoinColumn(name="book_id")
    Book book;

    @Column(name = "borrow_date")
    LocalDate borrowDate;

    @Column(name = "return_date")
    LocalDate returnDate;

    @Column(name = "status")
    String status;
}
