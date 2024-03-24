package com.fjut.noveltts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NovelTtsApplication {
    public static void main(String[] args) {
        SpringApplication.run(NovelTtsApplication.class, args);
        System.err.println("Server started: http://localhost:8000/handle/index");
    }
}
