package org.zerock.nextenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class CodeQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeQueryApplication.class, args);
    }
}