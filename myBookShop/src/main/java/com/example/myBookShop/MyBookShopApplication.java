package com.example.myBookShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyBookShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBookShopApplication.class, args);
    }
}
