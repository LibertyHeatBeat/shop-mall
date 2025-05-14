package com.buka.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buka.enums.CouponPublishEnum;
import com.buka.enums.BizCodeEnum;
import com.buka.enums.CouponStateEnum;
import com.buka.exception.BizException;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.CouponRecordDO;
import com.buka.service.CouponRecordService;
import com.buka.util.CommonUtil;
import com.buka.util.JsonData;
import com.buka.model.CouponDO;
import com.buka.mapper.CouponMapper;
import com.buka.model.CouponPage;
import com.buka.service.CouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.vo.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lhb
 * @since 2025-02-15
 */
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponDO> implements CouponService {

    @Autowired
    private CouponRecordService couponRecordService;
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * @Author: lhb
     * @Description: 分页查询已发布的优惠券
     * @DateTime: 上午11:35 2025/2/15
     * @Params:
     *   page - 当前页码，表示要查询的页数
     *   size - 每页显示的记录数，表示每页要返回的优惠券数量
     * @Return com.buka.util.JsonData - 返回一个包含分页查询结果的JsonData对象，其中包含优惠券列表、总记录数和总页数等信息
     */
    @Override
    public JsonData pageCoupon(long page, long size) {
        // 创建分页对象，设置当前页码和每页记录数
        Page<CouponDO> page1 = new Page<>(page, size);

        // 创建Lambda查询条件构造器，用于构建查询条件
        LambdaQueryWrapper<CouponDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 设置查询条件：只查询已发布的优惠券
        lambdaQueryWrapper.eq(CouponDO::getPublish, CouponPublishEnum.PUBLISH);

        // 设置排序条件：按创建时间降序排列
        lambdaQueryWrapper.orderByDesc(CouponDO::getCreateTime);

        // 执行分页查询，获取分页结果
        this.page(page1, lambdaQueryWrapper);

        // 从分页结果中获取当前页的优惠券记录
        List<CouponDO> records = page1.getRecords();

        // 获取总记录数
        Long total = page1.getTotal();

        // 获取总页数
        Long pages = page1.getPages();

        // 创建自定义分页对象，用于封装分页查询结果
        CouponPage<CouponDO> couponPage = new CouponPage<>();
        couponPage.setRecords(records);
        couponPage.setTotal(total);
        couponPage.setSize(pages);

        // 返回成功响应，包含分页查询结果
        return JsonData.buildSuccess(couponPage);
    }


    /**
    * @Author: lhb
    * @Description: 通过优惠券ID领取优惠券
    * @DateTime: 上午9:12 2025/2/17
    * @Params: [couponId]
    * @Return com.buka.util.JsonData
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData addPromotion(Long couponId) {
        // 获取分布式锁，防止并发领取优惠券
        RLock lock = redissonClient.getLock("lock:coupon:" + couponId);
        lock.lock(10,TimeUnit.SECONDS);
        log.info("加锁成功");
        try {
            //1,获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            //2，查询优惠
            CouponDO byId = this.getById(couponId);
            //3,判断优惠卷是否可用
            this.checkCoupon(byId, loginUser.getId());
            //4,扣减库存
            int rows = couponMapper.reduceStock(couponId);
            if (rows == 1) {
                //5,保存领劵记录
                //构建领劵记录
                CouponRecordDO couponRecordDO = new CouponRecordDO();
                BeanUtils.copyProperties(byId, couponRecordDO);
                couponRecordDO.setCreateTime(new Date());
                couponRecordDO.setUseState(CouponStateEnum.NEW.name());
                couponRecordDO.setUserId(loginUser.getId());
                couponRecordDO.setUserName(loginUser.getName());
                couponRecordDO.setCouponId(couponId);
                couponRecordDO.setId(null);
                couponRecordService.save(couponRecordDO);
            }
        }finally {
            lock.unlock();
        }
        return JsonData.buildSuccess();
    }

    /**
     * @Author: lhb
     * @Description: 检查优惠券是否可领取
     * @DateTime: 上午11:36 2025/2/15
     * @Params: [coupon, userId]
     * @Return void
     */
    public void checkCoupon(CouponDO coupon, Long userId){
        if (coupon==null){
            throw new BizException(BizCodeEnum.COUPON_NOT_EXIST);
        }
        if (coupon.getStock()<=0){
            throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
        }
        if (!coupon.getPublish().equals(CouponPublishEnum.PUBLISH.name())) {
            throw new BizException(BizCodeEnum.COUPON_GET_FAIL);
        }
        //是否在领取时间范围
        long start = coupon.getStartTime().getTime();
        long end = coupon.getEndTime().getTime();
        long ttl = System.currentTimeMillis();
        if (ttl < start || ttl > end){
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_TIME);
        }
        //用户是否超过限制
        LambdaQueryWrapper<CouponRecordDO> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CouponRecordDO::getUserId,userId);
        lambdaQueryWrapper.eq(CouponRecordDO::getCouponId,coupon.getId());
        int count = couponRecordService.count(lambdaQueryWrapper);
        if (count>=coupon.getUserLimit()){
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_LIMIT);
        }
    }
}
