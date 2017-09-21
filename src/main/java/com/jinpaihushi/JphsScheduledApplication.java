package com.jinpaihushi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class JphsScheduledApplication {

    public static void main(String[] args) {
        SpringApplication.run(JphsScheduledApplication.class, args);
    }
}
