package com.buka.request;

import lombok.Data;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/2/17 下午2:37
 */
@Data
public class NewUserCouponRequest {
    private long userId;
    private String name;
}
