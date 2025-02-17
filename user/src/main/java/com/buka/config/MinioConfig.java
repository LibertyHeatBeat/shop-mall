package com.buka.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @Description minio核心配置类
 *  * 通过注入 MinIO 服务器的相关配置信息，得到 MinioClient 对象，我们上传文件依赖此对象
 * @Author lhb
 * @Date 2025/2/16
 */
@Configuration
public class MinioConfig {

    @Autowired
    private MinioProp prop;

    /**
     * 获取 MinioClient
     * @return MinioClient
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().endpoint(prop.getEndpoint()).
                credentials(prop.getAccesskey(),prop.getSecretkwy()).build();
    }
}
