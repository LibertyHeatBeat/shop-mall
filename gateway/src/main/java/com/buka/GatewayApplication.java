package com.buka;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author lhb
 * @version 1.0
 * @description: 启动类
 * @date 2025/3/29 上午9:35
 */
@SpringBootApplication
@EnableDiscoveryClient//开启nacos服务注册和发现
public class GatewayApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(GatewayApplication.class, args);
    }
}
