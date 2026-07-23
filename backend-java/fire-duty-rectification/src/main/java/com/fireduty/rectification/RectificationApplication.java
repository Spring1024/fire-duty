package com.fireduty.rectification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.fireduty.rectification", "com.fireduty.common"})
public class RectificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(RectificationApplication.class, args);
    }
}
