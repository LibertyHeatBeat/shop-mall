package com.buka.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/9 下午7:59
 */
@Data
public class CouponRecordVO {


    private Long id;

    /**
     * 优惠券id
     */

    private Long couponId;


    /**
     * 使用状态  可用 NEW,已使用USED,过期 EXPIRED;
     */

    private String useState;

    /**
     * 用户id
     */

    private Long userId;

    /**
     * 用户昵称
     */

    private String userName;

    /**
     * 优惠券标题
     */

    private String couponTitle;

    /**
     * 开始时间
     */

    private Date startTime;

    /**
     * 结束时间
     */

    private Date endTime;

    /**
     * 订单id
     */

    private Long orderId;

    /**
     * 抵扣价格
     */
    private BigDecimal price;

    /**
     * 满多少才可以使用
     */

    private BigDecimal conditionPrice;


}
