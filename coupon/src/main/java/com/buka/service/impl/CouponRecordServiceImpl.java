package com.buka.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buka.enums.CouponCategoryEnum;
import com.buka.enums.CouponPublishEnum;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.CouponDO;
import com.buka.model.CouponRecordDO;
import com.buka.mapper.CouponRecordMapper;
import com.buka.request.NewUserCouponRequest;
import com.buka.service.CouponRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.service.CouponService;
import com.buka.util.JsonData;
import com.buka.vo.CouponRecordVO;
import com.buka.vo.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lhb
 * @since 2025-02-15
 */
@Service
public class CouponRecordServiceImpl extends ServiceImpl<CouponRecordMapper, CouponRecordDO> implements CouponRecordService {

    @Autowired
    private CouponService couponService;

    private final LoginInterceptor loginInterceptor;

    public CouponRecordServiceImpl(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    /**
    * @Author: lhb
    * @Description: 分页查询
    * @DateTime: 上午11:06 2025/2/17
    * @Params: [page, size]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData pageCouponRecord(long page, long size) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        Page<CouponRecordDO> page1 = new Page<>(page, size);
        LambdaQueryWrapper<CouponRecordDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CouponRecordDO::getUserId, loginUser.getId());
        lambdaQueryWrapper.orderByDesc(CouponRecordDO::getCreateTime);
        this.page(page1, lambdaQueryWrapper);
        List<CouponRecordDO> records = page1.getRecords();
        records.stream().map(obg -> {
            CouponRecordVO couponRecord = new CouponRecordVO();
            BeanUtils.copyProperties(obg, couponRecord);
            return couponRecord;
        }).collect(Collectors.toList());
        Long total = page1.getTotal();
        Long pages = page1.getPages();
        Map<String, Object> map = new HashMap<>();
        map.put("records", records);
        map.put("total", total);
        map.put("pages", pages);
        return JsonData.buildSuccess(map);
    }

    /**
    * @Author: lhb
    * @Description: 根据优惠卷id查询优惠卷
    * @DateTime: 下午2:40 2025/2/17
    * @Params: [recordId]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData detail(Long recordId) {
        LambdaQueryWrapper<CouponRecordDO> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CouponRecordDO::getId,recordId);
        lambdaQueryWrapper.eq(CouponRecordDO::getUserId,LoginInterceptor.threadLocal.get().getId());
        CouponRecordDO one = getOne(lambdaQueryWrapper);
        return one==null?JsonData.buildError("未查询到记录"):JsonData.buildSuccess(one);
    }

    /**
    * @Author: lhb
    * @Description: 新人发放优惠卷
    * @DateTime: 下午2:40 2025/2/17
    * @Params: [newUserCouponRequest]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData newUserCoupon(NewUserCouponRequest newUserCouponRequest) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(newUserCouponRequest.getUserId());
        loginUser.setName(newUserCouponRequest.getName());
        loginInterceptor.threadLocal.set(loginUser);
        LambdaQueryWrapper<CouponDO> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CouponDO::getCategory, CouponCategoryEnum.NEW_USER.name());
        lambdaQueryWrapper.eq(CouponDO::getPublish, CouponPublishEnum.PUBLISH.name());
        List<CouponDO> list = couponService.list(lambdaQueryWrapper);
        for (CouponDO couponDO : list) {
            couponService.addPromotion(couponDO.getId());
        }
        return JsonData.buildSuccess();
    }
}
