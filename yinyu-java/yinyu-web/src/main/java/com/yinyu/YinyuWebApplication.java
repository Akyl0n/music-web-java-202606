package com.yinyu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = {"com.yinyu.mapper"})
@SpringBootApplication(scanBasePackages = "com.yinyu")
public class YinyuWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(YinyuWebApplication.class, args);
    }
}
