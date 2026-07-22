package com.fireduty.rectification.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.fireduty.rectification.mapper")
public class MyBatisPlusConfig {
}
