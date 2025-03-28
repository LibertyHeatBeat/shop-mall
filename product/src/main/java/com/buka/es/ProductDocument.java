package com.buka.es;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lhb
 * @version 1.0
 * @description: 索引库类型
 * @date 2025/3/26 下午6:52
 */
@Data
@Document(indexName = "product_index", createIndex = true)
public class ProductDocument {
    /**
     * id
     */
    @Id
    @Field(type = FieldType.Keyword)
    private Long id;

    /**
     * 标题
     */
    @Field(type = FieldType.Text , analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 封面图
     */
    @Field(type = FieldType.Keyword)
    private String coverImg;

    /**
     * 详情
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String detail;

    /**
     * 老价格
     */
    @Field(type = FieldType.Double)
    private BigDecimal oldAmount;

    /**
     * 新价格
     */
    @Field(type = FieldType.Double)
    private BigDecimal amount;

    /**
     * 库存
     */
    @Field(type = FieldType.Integer)
    private Integer stock;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Keyword)
    private String createTime;

}
