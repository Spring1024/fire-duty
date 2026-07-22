package com.fireduty.grid.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.fireduty.grid.mapper")
public class MyBatisPlusConfig {
}
