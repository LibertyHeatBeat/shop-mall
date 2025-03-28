package com.buka.es;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buka.model.ProductDO;
import com.buka.service.ProductService;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lhb
 * @version 1.0
 * @description: 定时任务类
 * @date 2025/3/28 上午10:50
 */
@Component
public class Jod {
    @Autowired
    private ProductService productService;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
    * @Author: lhb
    * @Description: 定时任务：将数据库中最近一天内创建的产品数据同步到Elasticsearch中。
     * 该任务每20秒执行一次，通过分页查询数据库中的产品数据，并与Elasticsearch中的数据进行比对，
     * 将新增或更新的数据批量保存到Elasticsearch中。
    * @DateTime: 上午10:53 2025/3/28
    * @Params: []
    * @Return void
    */
    @Scheduled(cron = "0/20 * * * * ?")
    public void syncToES() {
        int pageSize = 100; // 每页查询的数据量
        int pageNum = 1;    // 当前查询的页码
        boolean flag = true; // 控制循环的标志

        while (flag) {
            // 分页查询数据库中最近一天内创建的产品数据
            Page<ProductDO> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ProductDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.ge(ProductDO::getCreateTime, LocalDateTime.now().minus(Duration.ofDays(1)));

            Page<ProductDO> page1 = productService.page(page, lambdaQueryWrapper);
            List<ProductDO> records = page1.getRecords();

            if (records.isEmpty()) {
                // 如果没有数据，则结束循环
                flag = false;
            } else {
                // 提取查询结果中的产品ID列表   1 ,2, 3, 4
                List<Object> ids = records.stream().map(obj -> obj.getId().toString()).collect(Collectors.toList());

                // 获取Elasticsearch中产品文档的索引名称
                IndexCoordinates indexName = elasticsearchRestTemplate.getIndexCoordinatesFor(ProductDocument.class);

                // 构建查询条件，查询Elasticsearch中是否存在这些产品ID对应的文档
                NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.termsQuery("id", ids)).build();

                //1 ,2 ,3 ,4
                SearchHits<ProductDocument> search = elasticsearchRestTemplate.search(nativeSearchQuery, ProductDocument.class, indexName);

                // 提取查询结果中的文档内容，并将其转换为Map，方便后续比对
                List<ProductDocument> esDocuments = search.stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList());

                //1  ,数据    2，数据   3，数据   4，null
                Map<Long, ProductDocument> collect = esDocuments.stream().collect(Collectors.toMap(pad -> pad.getId(), Function.identity()));

                // 遍历数据库查询结果，与Elasticsearch中的文档进行比对，生成需要更新的文档列表
                List<ProductDocument> documentsToUpdate = new ArrayList<>();
                for (ProductDO record : records) {
                    ProductDocument productDocument = collect.get(record.getId());
                    if (productDocument == null) {
                        // 如果Elasticsearch中不存在该文档，则创建新文档并设置创建时间
                        BeanUtils.copyProperties(record, productDocument);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm:ss");
                        String format = simpleDateFormat.format(record.getCreateTime());
                        productDocument.setCreateTime(format);
                        documentsToUpdate.add(productDocument);
                    }
                }

                // 如果有需要更新的文档，则批量保存到Elasticsearch中
                if (documentsToUpdate.size() > 0) {
                    elasticsearchRestTemplate.save(documentsToUpdate, indexName);
                }

                // 增加页码，继续查询下一页数据
                pageNum++;
            }
        }
    }
}
