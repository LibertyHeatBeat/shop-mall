package com.buka.service;

import com.buka.model.CouponDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buka.util.JsonData;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lhb
 * @since 2025-02-15
 */
public interface CouponService extends IService<CouponDO> {

    JsonData pageCoupon(long page, long size);

    JsonData addPromotion(Long couponId);
}
