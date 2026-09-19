package com.example.myBookShop;

import com.example.myBookShop.Entity.Book;
import com.example.myBookShop.Repositories.BookRepository;
import com.example.myBookShop.Service.UserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyBookShopApplication implements CommandLineRunner {

    @Autowired
    ApplicationContext applicationContext;
	public static void main(String[] args) {

		SpringApplication.run(MyBookShopApplication.class, args);

	}

    @Override
    public void run(String... args) throws Exception {
        UserService sv1 = applicationContext.getBean(UserService.class);
        UserService sv2 = applicationContext.getBean(UserService.class);

        if(sv1==sv2) System.out.println("TRUE");
    }
}
