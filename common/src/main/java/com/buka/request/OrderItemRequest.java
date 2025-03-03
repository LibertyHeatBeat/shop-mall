package com.buka.request;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/5 下午7:10
 */
@Data
@AllArgsConstructor
public class OrderItemRequest {
    /**
     * 商品id
     */
    private Long productId;
    /**
     * 商品数量
     */
    private Integer buyNum;
    public OrderItemRequest(){}
}
