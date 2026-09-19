package com.example.myBookShop.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "books")
public class Book {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int Id;

    @Column(name="title")
    String title;

    @Column(name="price")
    BigDecimal price;

    @Column(name="quantity")
    int  quantity;

    @JoinColumn(name="author_id")
    @ManyToOne(fetch = FetchType.EAGER)
    Author author;

    @OneToMany(mappedBy = "book")
    List<borrowRecord> borrowRecordList;

}
