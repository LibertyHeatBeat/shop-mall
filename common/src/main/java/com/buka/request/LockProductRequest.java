package com.buka.request;

import lombok.Data;

import java.util.List;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/5 下午7:11
 */
@Data
public class LockProductRequest {

    private String orderOutTradeNo;

    private List<OrderItemRequest> orderItemRequest;
}
