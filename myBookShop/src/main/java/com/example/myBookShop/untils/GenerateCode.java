package com.example.myBookShop.untils;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class GenerateCode {
    private final int MAX=100000;
    private final int MIN=1;


    public GenerateCode(){
    }
    public String genCode(){
        Random ran = new Random();
        int code = ran.nextInt(MAX-MIN+1)-MIN;
        return String.format("%06d",code);
    }
}
