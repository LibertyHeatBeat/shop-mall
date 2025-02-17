package com.buka.model;

import lombok.Data;

import java.util.List;

/**
 * @Description page分页实体类
 * @Author lhb
 * @Date 2025/2/15
 */
@Data
public class CouponPage<T> {
    private List<T> records;
    private long size;
    private long total;
}
