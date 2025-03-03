package com.buka.service;

import com.buka.model.CouponRecordDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buka.model.CouponRecordMessage;
import com.buka.request.LockCouponRecordRequest;
import com.buka.request.NewUserCouponRequest;
import com.buka.util.JsonData;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lhb
 * @since 2025-02-15
 */
public interface CouponRecordService extends IService<CouponRecordDO> {

    JsonData pageCouponRecord(long page, long size);

    JsonData detail(Long recordId);

    JsonData newUserCoupon(NewUserCouponRequest newUserCouponRequest);

    JsonData lockRecords(LockCouponRecordRequest lockRecords);

    boolean releaseCouponRecord(CouponRecordMessage recordMessage);
}
