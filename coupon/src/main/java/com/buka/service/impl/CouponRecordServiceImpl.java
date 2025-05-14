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
    * @Description: 分页个人查询优惠卷
    * @DateTime: 上午11:06 2025/2/17
    * @Params: [page, size]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData pageCouponRecord(long page, long size) {
        // 从线程局部变量中获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        // 创建分页对象，设置当前页码和每页大小
        Page<CouponRecordDO> page1 = new Page<>(page, size);

        // 创建查询条件对象，设置查询条件为当前用户的ID，并按创建时间降序排序
        LambdaQueryWrapper<CouponRecordDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CouponRecordDO::getUserId, loginUser.getId());
        lambdaQueryWrapper.orderByDesc(CouponRecordDO::getCreateTime);

        // 执行分页查询
        this.page(page1, lambdaQueryWrapper);

        // 获取查询结果中的记录列表
        List<CouponRecordDO> records = page1.getRecords();

        // 将查询结果中的DO对象转换为VO对象
        records.stream().map(obg -> {
            CouponRecordVO couponRecord = new CouponRecordVO();
            BeanUtils.copyProperties(obg, couponRecord);
            return couponRecord;
        }).collect(Collectors.toList());

        // 获取总记录数和总页数
        Long total = page1.getTotal();
        Long pages = page1.getPages();

        // 将查询结果、总记录数和总页数放入Map中
        Map<String, Object> map = new HashMap<>();
        map.put("records", records);
        map.put("total", total);
        map.put("pages", pages);

        // 返回包含查询结果的JSON数据
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
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        lambdaQueryWrapper.eq(CouponRecordDO::getUserId,loginUser.getId());
        CouponRecordDO one = getOne(lambdaQueryWrapper);
        return one==null?JsonData.buildError("未查询到记录"):JsonData.buildSuccess(one);
    }

    /**
    * @Author: lhb
    * @Description: 新人发放优惠卷
    * @DateTime: 下午2:40 2025/2/17
    * @Params: [newUserCouponRequest] 包含新用户信息的请求对象，用于获取用户ID和名称
    * @Return com.buka.util.JsonData 返回操作结果，成功时返回成功状态
    */
    @Override
    public JsonData newUserCoupon(NewUserCouponRequest newUserCouponRequest) {
        // 创建并设置登录用户信息，用于后续操作
        LoginUser loginUser = new LoginUser();
        loginUser.setId(newUserCouponRequest.getUserId());
        loginUser.setName(newUserCouponRequest.getName());
        loginInterceptor.threadLocal.set(loginUser);

        // 构建查询条件，筛选出适用于新用户的已发布优惠券
        LambdaQueryWrapper<CouponDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CouponDO::getCategory, CouponCategoryEnum.NEW_USER.name());
        lambdaQueryWrapper.eq(CouponDO::getPublish, CouponPublishEnum.PUBLISH.name());
        List<CouponDO> list = couponService.list(lambdaQueryWrapper);

        // 遍历符合条件的优惠券，为每个优惠券添加促销活动
        for (CouponDO couponDO : list) {
            couponService.addPromotion(couponDO.getId());
        }

        // 返回操作成功的结果
        return JsonData.buildSuccess();
    }

    /**
     * @Author: lhb
     * @Description: 锁定优惠券记录，将指定的优惠券记录标记为已使用，并创建相应的任务记录和发送延迟消息。
     * @DateTime: 下午5:14 2025/4/24
     * @Params: [lockRecords] 包含需要锁定的优惠券记录信息以及外部交易号的请求对象
     * @Return com.buka.util.JsonData 返回操作结果，成功时返回成功的JsonData对象
     */
    @Override
    public JsonData lockRecords(LockCouponRecordRequest lockRecords) {
        // 获取当前登录用户信息
        LoginUser loginUser =LoginInterceptor.threadLocal.get();
        // 获取外部交易号
        String outTradeNo = lockRecords.getOutTradeNo();
        // 获取需要锁定的优惠券记录ID列表
        List<Long> couponRecordIds = lockRecords.getCouponRecordIds();

        // 如果优惠券记录ID列表不为空，则逐个处理
        if (couponRecordIds != null){
            for(Long couponRecordId : couponRecordIds){
                // 构建更新条件，将优惠券记录标记为已使用
                LambdaUpdateWrapper<CouponRecordDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapper.eq(CouponRecordDO::getId, couponRecordId);
                lambdaUpdateWrapper.eq(CouponRecordDO::getUserId, loginUser.getId());
                lambdaUpdateWrapper.set(CouponRecordDO::getUseState, CouponStateEnum.USED.name());
                boolean update = update(lambdaUpdateWrapper);

                // 如果更新失败，抛出业务异常
                if(!update){
                    throw new BizException(BizCodeEnum.COUPON_LOCK_FAIL);
                }

                // 创建优惠券任务记录，并保存到数据库
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
        // 返回操作成功的JsonData对象
        return JsonData.buildSuccess();
    }


    /**
     * @Author: lhb
     * @Description: 根据传入的优惠券记录消息，释放优惠券记录。该方法会检查工作单状态和订单状态，
     *               并根据订单状态更新工作单和优惠券记录的状态。如果订单状态为NEW，则返回false，
     *               表示需要重新投递消息；如果订单状态为PAY，则更新工作单状态为FINISH；如果订单
     *               不存在或已取消，则更新工作单状态为CANCEL，并恢复优惠券使用记录为NEW。
     * @DateTime: 下午5:14 2025/4/24
     * @Params: [recordMessage] 优惠券记录消息，包含任务ID和外部交易号等信息
     * @Return boolean 返回true表示处理成功，返回false表示需要重新投递消息
     */
    @Override
    public boolean releaseCouponRecord(CouponRecordMessage recordMessage) {
        Long taskId = recordMessage.getTaskId();
        String outTradeNo = recordMessage.getOutTradeNo();

        // 根据任务ID获取工作单信息
        CouponTaskDO couponTaskDO = couponTaskService.getById(taskId);
        if (couponTaskDO == null){
            log.warn("工作单不存，消息:{}", recordMessage);
            return true;
        }

        // 检查工作单是否处于锁定状态
        if (couponTaskDO.getLockState().equalsIgnoreCase(CouponTaskStateEnum.LOCK.name())){
            // 查询订单状态
            JsonData jsonData = productOrderFeignSerivce.queryProductOrderState(outTradeNo);
            if (jsonData.getCode() == 0){
                String data = jsonData.getData().toString();
                // 如果订单状态为NEW，返回false，表示需要重新投递消息
                if (data.equalsIgnoreCase(ProductOrderStateEnum.NEW.name())){
                    log.warn("订单状态是NEW,返回给消息队列，重新投递:{}", recordMessage);
                    return false;
                }
                // 如果订单状态为PAY，更新工作单状态为FINISH
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
