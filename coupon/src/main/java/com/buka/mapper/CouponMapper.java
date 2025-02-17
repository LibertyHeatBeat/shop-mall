package com.buka.mapper;

import com.buka.model.CouponDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lhb
 * @since 2025-02-15
 */
public interface CouponMapper extends BaseMapper<CouponDO> {

    int reduceStock(Long couponId);
}
