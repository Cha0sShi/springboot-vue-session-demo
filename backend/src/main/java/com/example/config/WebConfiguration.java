package com.example.config;

import com.example.interceptor.AuthorizeInterceptor;
import jakarta.annotation.Resource;
import org.apache.ibatis.plugin.Intercepts;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Resource
    HandlerInterceptor interceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/**");;
    }
}
