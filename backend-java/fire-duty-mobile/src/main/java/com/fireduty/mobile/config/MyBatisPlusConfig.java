package com.fireduty.mobile.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.fireduty.mobile.mapper")
public class MyBatisPlusConfig {
}
