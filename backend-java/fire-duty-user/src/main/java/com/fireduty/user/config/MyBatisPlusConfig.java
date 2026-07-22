package com.fireduty.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.fireduty.user.mapper")
public class MyBatisPlusConfig {
}
