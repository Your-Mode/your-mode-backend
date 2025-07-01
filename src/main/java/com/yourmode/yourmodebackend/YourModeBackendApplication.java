package com.yourmode.yourmodebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.yourmode.yourmodebackend.domain.user.mapper")
public class YourModeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YourModeBackendApplication.class, args);
    }

}
