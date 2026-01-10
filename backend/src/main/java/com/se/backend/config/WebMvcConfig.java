package com.se.backend.config;

import com.se.backend.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    /**
     * 配置跨域 (CORS)
     * 修复点：将 allowedOrigins("*") 替换为 allowedOriginPatterns("*")
     * 以解决 "When allowCredentials is true, allowedOrigins cannot contain *" 的报错
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 使用 Pattern 匹配所有域名，兼容 allowCredentials
                .allowedOriginPatterns("*") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // 允许前端带 Token/Cookie
                .maxAge(3600);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                // 排除登录注册、静态资源、Swagger等接口
                .excludePathPatterns(
                        "/user/login", 
                        "/user/register", 
                        "/error",
                        "/static/**",
                        "/speech/transcribe" // 视情况可选排除，一般需鉴权
                );
    }

    /**
     * 配置静态资源映射 (用于访问上传的音频)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /static/audio/** 映射到本地 uploads 目录
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        registry.addResourceHandler("/static/audio/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
