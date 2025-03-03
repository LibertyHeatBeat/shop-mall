package com.buka.controller;


import com.buka.request.LockCouponRecordRequest;
import com.buka.request.NewUserCouponRequest;
import com.buka.service.CouponRecordService;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lhb
 * @since 2025-02-15
 */
@RestController
@RequestMapping("/api/couponRecord/v1")
public class CouponRecordController {
    @Autowired
    private CouponRecordService couponRecordService;

    @GetMapping("page_couponRecord")
    public JsonData pageCouponRecord(@RequestParam(value = "page") long page,
                                     @RequestParam(value = "size") long size) {
        return couponRecordService.pageCouponRecord(page, size);
    }

    /**
    * @Author: lhb
    * @Description: 根据记录ID获取优惠券详细信息
    * @DateTime: 下午2:34 2025/2/17
    * @Params: [recordId]
    * @Return com.buka.util.JsonData
    */
    @GetMapping("/detail/{record_id}")
    public JsonData detail(@PathVariable("record_id") Long recordId){
        return couponRecordService.detail(recordId);
    }

    @PostMapping("/new_user_coupon")
    public JsonData newUserCoupon(@RequestBody NewUserCouponRequest newUserCouponRequest){
        return couponRecordService.newUserCoupon(newUserCouponRequest);
    }

    /**
    * @Author: lhb
    * @Description: 锁定优惠卷
    * @DateTime: 下午8:26 2025/3/13
    * @Params: [lockRecords]
    * @Return com.buka.util.JsonData
    */
    @PostMapping("lock_records")
    public JsonData lockRecords(@RequestBody LockCouponRecordRequest lockRecords) {
        return couponRecordService.lockRecords(lockRecords);
    }
}

