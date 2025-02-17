package com.buka.controller;


import com.buka.service.CouponService;
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
@RequestMapping("/api/coupon/v1")
public class CouponController {
    @Autowired
    private CouponService couponService;


    @RequestMapping("/test")
    public String test() {
        return couponService.list().toString();
    }

    /**
     * @Author: lhb
     * @Description: 分页获取优惠券信息
     * @DateTime: 上午10:35 2025/2/15
     * @Params: [page, size]
     * @Return java.lang.String
     */
    @GetMapping("page_coupon")
    public String pageCoupon(@RequestParam(value = "page")String page,
                             @RequestParam(value = "size")String size){
        JsonData jsonData = couponService.pageCoupon(Integer.parseInt(page),Integer.parseInt(size));
        return jsonData.getData().toString();
    }

    /**
    * @Author: lhb
    * @Description: 添加推广优惠卷
    * @DateTime: 上午11:34 2025/2/15
    * @Params: [couponId]
    * @Return com.buka.util.JsonData
    */
    @PutMapping("/add/promotion/{coupon_id}")
    public JsonData addPromotion(@PathVariable("coupon_id") Long couponId) {
        return couponService.addPromotion(couponId);
    }


}

