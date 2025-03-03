package com.buka.model;

import lombok.Data;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/7 下午4:04
 */
@Data
public class ProductMessage {
    /**
     * 消息队列id
     */
    private Long messageId;


    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 库存锁定工作单id
     */
    private Long taskId;
}
