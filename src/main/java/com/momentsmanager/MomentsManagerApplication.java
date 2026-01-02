package com.momentsmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"com.momentsmanager.event", "com.momentsmanager.user", "com.momentsmanager.model"})
public class MomentsManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MomentsManagerApplication.class, args);
    }
}
