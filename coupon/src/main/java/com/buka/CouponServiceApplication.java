package com.buka;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Description 启动类
 * @Author lhb
 * @Date 2025/2/15
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.buka.mapper")
@EnableFeignClients
@EnableDiscoveryClient
public class CouponServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponServiceApplication.class,args);
    }
}
