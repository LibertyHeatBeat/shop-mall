package com.buka.config;

import com.buka.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description 拦截器配置类
 * @Author lhb
 * @Date 2025/2/15
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {


    LoginInterceptor loginInterceptor(){
        return new LoginInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/api/coupon_record/*/**")
                .addPathPatterns("/api/coupon/*/**")
                .excludePathPatterns("/api/coupon/*/page_coupon","/api/coupon/*/new_user_coupon");


        WebMvcConfigurer.super.addInterceptors(registry);
    }
}