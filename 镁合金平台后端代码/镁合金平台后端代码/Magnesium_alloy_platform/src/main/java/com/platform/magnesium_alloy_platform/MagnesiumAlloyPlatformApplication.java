package com.platform.magnesium_alloy_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;

@SpringBootApplication(exclude = {MultipartAutoConfiguration.class})
public class MagnesiumAlloyPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagnesiumAlloyPlatformApplication.class, args);
    }

}
