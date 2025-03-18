package com.buka.model;

import lombok.Data;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/18 下午4:13
 */
@Data
public class OrderMessage {
    /**
     * 消息队列id
     */
    private Long messageId;


    /**
     * 订单号
     */
    private String outTradeNo;
}
