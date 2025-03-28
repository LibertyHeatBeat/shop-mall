package com.buka.es;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;

import javax.annotation.PostConstruct;

/**
 * @author lhb
 * @version 1.0
 * @description: 初始化Elasticsearch索引的配置类。该类在应用启动时自动执行，用于创建和映射Elasticsearch索引。
 * @date 2025/3/28 上午10:35
 */
@Configuration
@Slf4j
public class InitIndex {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
    * @Author: lhb
    * @Description: 初始化Elasticsearch索引的方法。该方法在Spring容器初始化完成后自动执行。
     * 主要功能是检查并创建ProductDocument类对应的索引，如果索引不存在则创建并映射。
    * @DateTime: 上午10:41 2025/3/28
    * @Params: []
    * @Return void
    */
    @PostConstruct
    public void initIndex() {
        log.info("初始化索引");
        try {
            // 获取ProductDocument类对应的索引操作对象
            IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(ProductDocument.class);

            // 检查索引是否存在，如果不存在则创建索引并映射
            if (!indexOperations.exists()) {
                indexOperations.create();
                indexOperations.putMapping(indexOperations.createMapping());
            }
        } catch (Exception e) {
            // 记录初始化索引失败的异常信息
            log.error("初始化索引失败", e);
        }
    }
}
