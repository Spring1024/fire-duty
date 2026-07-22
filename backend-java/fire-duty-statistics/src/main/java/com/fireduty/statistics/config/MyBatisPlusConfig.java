package com.fireduty.statistics.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.fireduty.statistics.mapper")
public class MyBatisPlusConfig {
}
