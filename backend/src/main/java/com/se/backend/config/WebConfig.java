package com.se.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 【核心修复】使用 Paths.get(...).toUri().toString() 自动处理不同系统的路径分隔符和 file协议前缀
        // 结果示例(Windows): file:///D:/YourProject/uploads/
        // 结果示例(Mac/Linux): file:/home/user/project/uploads/
        String uploadPath = Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString();

        // 建立映射：当访问 /static/audio/** 时，去本地 uploads 文件夹找
        registry.addResourceHandler("/static/audio/**")
                .addResourceLocations(uploadPath);
    }
}
