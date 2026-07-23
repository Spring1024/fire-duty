package com.fireduty.grid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.fireduty.grid", "com.fireduty.common"})
public class GridApplication {
    public static void main(String[] args) {
        SpringApplication.run(GridApplication.class, args);
    }
}
