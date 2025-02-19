package com.buka.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/2/19 上午10:46
 */
@Data
public class ConfirmOrderDto {

    //优惠券id
    private Long couponRecordId;

    //商品id列表
    private List<Long> productIdList;

    //支付方式
    private String payType;

    //客户端类型
    private String clientType;

    //地址id
    private long addressId;

    //总金额
    private BigDecimal totalAmount;

    //实际支付金额
    private BigDecimal realPayAmount;

    //token防止重复提交
    private String token;
}
