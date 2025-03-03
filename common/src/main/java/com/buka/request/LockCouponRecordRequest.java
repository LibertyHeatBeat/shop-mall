package com.buka.request;

import lombok.Data;

import java.util.List;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/9 下午4:55
 */
@Data
public class LockCouponRecordRequest {
    private String outTradeNo;
    private List<Long> couponRecordIds;
}
