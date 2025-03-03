package com.buka.feign;

import com.buka.request.LockCouponRecordRequest;
import com.buka.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/9 下午8:01
 */
@FeignClient(name = "coupon-service")
public interface CouponFeignService {

    @GetMapping("/api/couponRecord/v1/detail/{record_id}")
    JsonData getCouponRecordDetail(@PathVariable("record_id") Long recordId);
    @PostMapping("/api/couponRecord/v1/lock_records")
    JsonData lockCouponRecords(LockCouponRecordRequest lockCouponRecordRequest);
}
