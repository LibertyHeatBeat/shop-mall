package com.buka.fegin;

import com.buka.request.NewUserCouponRequest;
import com.buka.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/2/19 下午3:11
 */
@FeignClient(name = "coupon-service")
public interface CouponFeignService {

    @PostMapping("/api/couponRecord/v1/new_user_coupon")
    JsonData newUserCoupon(NewUserCouponRequest newUserCouponRequest);
}
