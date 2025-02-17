package com.buka.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/2/17 下午3:58
 */
@Data
public class ProductVO  {


    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 封面图
     */

    private String coverImg;

    /**
     * 详情
     */
    private String detail;

    /**
     * 老价格
     */

    private BigDecimal oldPrice;

    /**
     * 新价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;




}
