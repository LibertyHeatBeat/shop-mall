package com.buka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description MinIO 配置属性类
 * @Author lhb
 * @Date 2025/2/16
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProp {
    //连接url
    private String endpoint;
    //公钥
    private String accesskey;
    //私钥
    private  String secretkwy;
}
