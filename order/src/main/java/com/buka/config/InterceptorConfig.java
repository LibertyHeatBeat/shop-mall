package com.buka.config;

import com.buka.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * @author lhb
 * @version 1.0
 * @description: 自定义拦截器
 * @date 2025/2/19 上午10:19
 */
@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginInterceptor())
                //拦截的路径
                .addPathPatterns("/api/order/*/**")

                //排查不拦截的路径
                .excludePathPatterns("/api/callback/*/**","/api/order/*/query_state");

    }
}
