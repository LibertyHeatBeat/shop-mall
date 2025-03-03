package com.buka.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buka.config.RabbitMQConfig;
import com.buka.enums.*;
import com.buka.exception.BizException;
import com.buka.feign.ProductOrderFeignSerivce;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.CouponDO;
import com.buka.model.CouponRecordDO;
import com.buka.mapper.CouponRecordMapper;
import com.buka.model.CouponRecordMessage;
import com.buka.model.CouponTaskDO;
import com.buka.request.LockCouponRecordRequest;
import com.buka.request.NewUserCouponRequest;
import com.buka.service.CouponRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.service.CouponService;
import com.buka.service.CouponTaskService;
import com.buka.util.JsonData;
import com.buka.vo.CouponRecordVO;
import com.buka.vo.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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
@Slf4j
public class CouponRecordServiceImpl extends ServiceImpl<CouponRecordMapper, CouponRecordDO> implements CouponRecordService {

    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponTaskService couponTaskService;
    @Autowired
    private RabbitMQConfig rabbitMQConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ProductOrderFeignSerivce productOrderFeignSerivce;


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

    @Override
    public JsonData lockRecords(LockCouponRecordRequest lockRecords) {
        LoginUser loginUser =LoginInterceptor.threadLocal.get();
        String outTradeNo = lockRecords.getOutTradeNo();
        List<Long> couponRecordIds = lockRecords.getCouponRecordIds();
        if (couponRecordIds != null){
            for(Long couponRecordId : couponRecordIds){
                LambdaUpdateWrapper<CouponRecordDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapper.eq(CouponRecordDO::getId, couponRecordId);
                lambdaUpdateWrapper.eq(CouponRecordDO::getUserId, loginUser.getId());
                lambdaUpdateWrapper.set(CouponRecordDO::getUseState, CouponStateEnum.USED.name());
                boolean update = update(lambdaUpdateWrapper);
                if(!update){
                    throw new BizException(BizCodeEnum.COUPON_LOCK_FAIL);
                }
                CouponTaskDO couponTaskDO = new CouponTaskDO();
                couponTaskDO.setCouponRecordId(couponRecordId);
                couponTaskDO.setOutTradeNo(outTradeNo);
                couponTaskDO.setLockState(CouponTaskStateEnum.LOCK.name());
                couponTaskDO.setCreateTime(new Date());
                couponTaskService.save(couponTaskDO);
                // 发送延迟消息，用于后续处理
                CouponRecordMessage couponRecordMessage = new CouponRecordMessage();
                couponRecordMessage.setOutTradeNo(outTradeNo);
                couponRecordMessage.setTaskId(couponTaskDO.getId());
                rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(), rabbitMQConfig.getCouponReleaseDelayRoutingKey(), couponRecordMessage);
            }
        }
        return JsonData.buildSuccess();
    }

    @Override
    public boolean releaseCouponRecord(CouponRecordMessage recordMessage) {
        Long taskId = recordMessage.getTaskId();
        String outTradeNo = recordMessage.getOutTradeNo();

        CouponTaskDO couponTaskDO = couponTaskService.getById(taskId);
        if (couponTaskDO == null){
            log.warn("工作单不存，消息:{}", recordMessage);
            return true;
        }
        if (couponTaskDO.getLockState().equalsIgnoreCase(CouponTaskStateEnum.LOCK.name())){
            JsonData jsonData = productOrderFeignSerivce.queryProductOrderState(outTradeNo);
            if (jsonData.getCode() == 0){
                String data = jsonData.getData().toString();
                if (data.equalsIgnoreCase(ProductOrderStateEnum.NEW.name())){
                    log.warn("订单状态是NEW,返回给消息队列，重新投递:{}", recordMessage);
                    return false;
                }
                if (data.equalsIgnoreCase(ProductOrderStateEnum.PAY.name())){
                    couponTaskDO.setLockState(CouponTaskStateEnum.FINISH.name());
                    couponTaskService.updateById(couponTaskDO);
                    return true;
                }
            }
            // 订单不存在或已取消，更新工作单状态为CANCEL，并恢复优惠券使用记录为NEW
            couponTaskDO.setLockState(StockTaskStateEnum.CANCEL.name());
            couponTaskService.updateById(couponTaskDO);

            LambdaUpdateWrapper<CouponRecordDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(CouponRecordDO::getId, couponTaskDO.getCouponRecordId());
            lambdaUpdateWrapper.set(CouponRecordDO::getUseState, CouponStateEnum.NEW.name());
            update(lambdaUpdateWrapper);
        }
        return true;
    }
}
