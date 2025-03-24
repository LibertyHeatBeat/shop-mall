package com.buka.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.buka.config.RabbitMQConfig;
import com.buka.constant.CacheKey;
import com.buka.dto.ConfirmOrderDto;
import com.buka.dto.PayInfoDTO;
import com.buka.enums.CouponStateEnum;
import com.buka.enums.ProductOrderStateEnum;
import com.buka.enums.BizCodeEnum;
import com.buka.exception.BizException;
import com.buka.feign.CouponFeignService;
import com.buka.feign.ProductFeignService;
import com.buka.feign.UserFeignService;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.*;
import com.buka.mapper.ProductOrderMapper;
import com.buka.pay.PayFactory;
import com.buka.request.LockCouponRecordRequest;
import com.buka.request.LockProductRequest;
import com.buka.request.OrderItemRequest;
import com.buka.service.ProductOrderItemService;
import com.buka.service.ProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.util.CommonUtil;
import com.buka.util.JsonData;
import com.buka.vo.CartItemVO;
import com.buka.vo.CouponRecordVO;
import com.buka.vo.LoginUser;
import com.buka.vo.OrderAddrVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lhb
 * @since 2025-02-19
 */
@Service
@Slf4j
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrderDO> implements ProductOrderService {

    @Autowired
    private UserFeignService userFeignService;
    @Autowired
    private ProductOrderItemService  productOrderItemService;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private RabbitMQConfig rabbitMQConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private PayFactory payFactory;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
    * @Author: lhb
    * @Description: 提交订单实现方法
    * @DateTime: 下午4:23 2025/3/23
    * @Params: [confirmOrderDto]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData confirmOrder(ConfirmOrderDto confirmOrderDto) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        // 校验令牌 防止重复提交
        String orderToken = confirmOrderDto.getToken();
        if(StringUtils.isBlank(orderToken)){
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_TOKEN_NOT_EXIST);
        }
        //原子操作 校验令牌，删除令牌
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] " +
                "then return redis.call('del',KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList(String.format(CacheKey.SUBMIT_ORDER_TOKEN_KEY,loginUser.getId())),orderToken);
        if(result == 0L){
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_TOKEN_EQUAL_FAIL);
        }
        //生成订单
        String orderOutTradeNo = CommonUtil.getStringNumRandom(32);

        //获取收货地址
        OrderAddrVo orderAddrVo = this.getUserAddr(confirmOrderDto.getAddressId());
        log.info("收货地址：{}", JSON.toJSONString(orderAddrVo));

        //获取最新购物项和价格
        List<CartItemVO> voList= getProduct(confirmOrderDto.getProductIdList());
        log.info("最新购物项和价格：{}", JSON.toJSONString(voList));
        // 订单校验
        this.checkPrice(confirmOrderDto, voList);

        //锁定优惠卷
        this.lockCouponRecords(confirmOrderDto.getCouponRecordId() ,orderOutTradeNo );

        //锁定库存
        this.lockProductStocks(voList,orderOutTradeNo);

        //创建订单
        ProductOrderDO productOrderDO = this.saveProductOrder(confirmOrderDto, loginUser, orderOutTradeNo, orderAddrVo);

        //创建订单项
        this.saveProductOrderItems(orderOutTradeNo, productOrderDO.getId(),voList);

        //自动关单
        this.sendDelayMessage(orderOutTradeNo);

        return null;
    }

    /**
    * @Author: lhb
    * @Description:自动关单方法
    * @DateTime: 下午4:09 2025/3/18
    * @Params: [orderOutTradeNo]
    * @Return void
    */
    private void sendDelayMessage(String orderOutTradeNo) {
        OrderMessage orderMessage=new OrderMessage();
        orderMessage.setOutTradeNo(orderOutTradeNo);
        rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(), rabbitMQConfig.getOrderCloseDelayRoutingKey(), orderMessage);
    }

    /**
    * @Author: lhb
    * @Description: 保存商品订单项信息
     * 该函数将购物车项列表转换为商品订单项列表，并批量保存到数据库中。
    * @DateTime: 上午11:17 2025/3/14
    * @Params: [orderOutTradeNo, id, voList]
    * @Return void
    */
    private void saveProductOrderItems(String orderOutTradeNo, Long id, List<CartItemVO> voList) {
        List<ProductOrderItemDO> productOrderDOList = voList.stream().map(obj -> {
            // 将购物车项列表转换为商品订单项列表
            ProductOrderItemDO productOrderItemDO = new ProductOrderItemDO();
            productOrderItemDO.setProductId(obj.getProductId());
            productOrderItemDO.setProductName(obj.getProductTitle());
            productOrderItemDO.setProductImg(obj.getProductImage());
            productOrderItemDO.setBuyNum(obj.getBuyNum());
            productOrderItemDO.setOutTradeNo(orderOutTradeNo);
            productOrderItemDO.setProductOrderId(id);
            productOrderItemDO.setTotalAmount(obj.getTotalAmount());
            productOrderItemDO.setAmount(obj.getProductPrice());
            return productOrderItemDO;
        }).collect(Collectors.toList());
        // 批量保存商品订单项到数据库
        productOrderItemService.saveBatch(productOrderDOList);
    }

    /**
    * @Author: lhb
    * @Description: 保存产品订单信息
     * 该函数用于根据确认订单的数据、用户信息、订单外部交易号和订单地址信息，生成并保存产品订单对象
    * @DateTime: 上午11:17 2025/3/14
    * @Params: [confirmOrderDto, loginUser, orderOutTradeNo, orderAddrVo]
    * @Return com.buka.model.ProductOrderDO
    */
    private ProductOrderDO saveProductOrder(ConfirmOrderDto confirmOrderDto, LoginUser loginUser, String orderOutTradeNo, OrderAddrVo orderAddrVo) {
        // 创建新的产品订单对象，并设置相关属性
        ProductOrderDO productOrderDO = new ProductOrderDO();
        productOrderDO.setOutTradeNo(orderOutTradeNo);
        productOrderDO.setState(ProductOrderStateEnum.NEW.name());
        productOrderDO.setPayType(confirmOrderDto.getPayType());
        productOrderDO.setTotalAmount(confirmOrderDto.getTotalAmount());
        productOrderDO.setPayAmount(confirmOrderDto.getRealPayAmount());
        productOrderDO.setOrderType("DAILY");
        productOrderDO.setUserId(loginUser.getId());
        productOrderDO.setReceiverAddress(JSON.toJSONString(orderAddrVo));
        productOrderDO.setNickname(loginUser.getName());
        productOrderDO.setHeadImg(loginUser.getHeadImg());

        // 保存产品订单对象到数据库
        save(productOrderDO);

        return productOrderDO;
    }

    /**
    * @Author: lhb
    * @Description: 锁定商品库存
     * 该函数用于将购物车中的商品信息转换为锁定库存请求，并调用商品微服务进行库存锁定操作。
     * 如果锁定库存失败，则抛出运行时异常。
    * @DateTime: 下午8:30 2025/3/13
    * @Params: [productIdList, orderOutTradeNo]
    * @Return void
    */
    private void lockProductStocks(List<CartItemVO> voList, String orderOutTradeNo) {
        // 将购物车中的商品信息转换为锁定库存请求
        List<OrderItemRequest> collect = voList.stream().map(obj -> {
            OrderItemRequest request = new OrderItemRequest();
            request.setProductId(obj.getProductId());
            request.setBuyNum(obj.getBuyNum());
            return request;
        }).collect(Collectors.toList());

        // 创建锁定库存请求
        LockProductRequest lockProduct = new LockProductRequest();
        lockProduct.setOrderOutTradeNo(orderOutTradeNo);
        lockProduct.setOrderItemRequest(collect);

        // 调用商品微服务接口，锁定库存
        JsonData jsonData = productFeignService.lockProductStocks(lockProduct);
        if (jsonData.getCode() != 0) {
            log.error("[商品微服务]-锁定库存失败");
            throw new RuntimeException("[商品微服务]-锁定库存失败");
        }
    }

    /**
    * @Author: lhb
    * @Description: 锁定指定的优惠券记录。
     * 该函数用于根据传入的优惠券记录ID和订单外部交易号，锁定对应的优惠券记录。
     * 如果优惠券记录ID为空或小于0，则直接返回，不执行任何操作。
     * 锁定操作通过调用优惠券微服务的接口完成，如果锁定失败，则抛出运行时异常。
    * @DateTime: 下午8:27 2025/3/13
    * @Params: [couponRecordId, orderOutTradeNo]
    * @Return void
    */
    private void lockCouponRecords(Long couponRecordId, String orderOutTradeNo) {
        if(couponRecordId!=null || couponRecordId<0){
            return;
        }
        LockCouponRecordRequest lockCouponRecordRequest = new LockCouponRecordRequest();
        lockCouponRecordRequest.setOutTradeNo(orderOutTradeNo);
        List<Long> list = new ArrayList<>();
        list.add(couponRecordId);
        lockCouponRecordRequest.setCouponRecordIds(list);
        JsonData jsonData = couponFeignService.lockCouponRecords(lockCouponRecordRequest);
        if (jsonData.getCode()!=0){
            log.error("[优惠券微服务]-锁定优惠卷失败");
            throw new RuntimeException("[优惠券微服务]-锁定优惠卷失败");
        }

    }

    /**
    * @Author: lhb
    * @Description: 获取收货地址
    * @DateTime: 下午8:27 2025/3/13
    * @Params: [addressId]
    * @Return com.buka.vo.OrderAddrVo
    */
    private OrderAddrVo getUserAddr(long addressId) {
        JsonData jsonData=userFeignService.find(addressId);

        if (jsonData.getCode()!=0){
            log.error("[用户微服务]-确认收货地址失败");
            throw new RuntimeException("[用户微服务]-确认收货地址失败");
        }
        OrderAddrVo orderAddrVo= jsonData.getData(new TypeReference<OrderAddrVo>(){});

        return orderAddrVo;

    }

    /**
    * @Author: lhb
    * @Description: 
    * @DateTime: 下午8:27 2025/3/13
    * @Params: [productIdList]
    * @Return java.util.List<com.buka.vo.CartItemVO>
    */
    private List<CartItemVO> getProduct(List<Long> productIdList) {
        // 调用商品微服务接口，获取购物项和价格信息
        JsonData jsonData = productFeignService.confirmOrderCartItems(productIdList);

        // 检查返回的JsonData对象，如果状态码不为0，表示获取数据失败
        if (jsonData.getCode() != 0) {
            log.error("[商品微服务]-获取最新购物项和价格失败");
            throw new RuntimeException("[商品微服务]-获取最新购物项和价格失败");
        }

        // 将JsonData中的数据转换为List<CartItemVO>对象
        List<CartItemVO> productVoList = jsonData.getData(new TypeReference<List<CartItemVO>>(){});

        return productVoList;
    }

    /**
    * @Author: lhb
    * @Description: 校验订单价格
    * @DateTime: 下午2:29 2025/3/24
    * @Params: [confirmOrderDto, voList]
    * @Return void
    */
    private void checkPrice(ConfirmOrderDto confirmOrderDto, List<CartItemVO> voList) {
        //最新总金额
        BigDecimal totalAmount = new BigDecimal("0");
        if (voList != null) {
            for (CartItemVO cartItemVO : voList) {
                totalAmount = totalAmount.add(cartItemVO.getTotalAmount());
            }
        }

        //获取优惠卷
        CouponRecordVO couponRecordVO = this.getCartCouponRecord(confirmOrderDto.getCouponRecordId());

        if (couponRecordVO!=null){
            //计算是否满足满减
            if (totalAmount.compareTo(couponRecordVO.getConditionPrice())<0){
                log.error("[优惠券微服务]-优惠券状态异常");
                throw new RuntimeException("[优惠券微服务]-优惠券状态异常");
            }

            if(couponRecordVO.getPrice().compareTo(totalAmount)>0){
                totalAmount = BigDecimal.ZERO;
            }else {
                totalAmount = totalAmount.subtract(couponRecordVO.getPrice());
            }
        }


        if (totalAmount.compareTo(confirmOrderDto.getRealPayAmount())!=0){
            log.error("[订单验价]-订单验价失败");
            throw new RuntimeException("[订单验价]-订单验价失败");
        }

    }

    /**
    * @Author: lhb
    * @Description: 远程调用优惠卷
    * @DateTime: 下午2:29 2025/3/24
    * @Params: [couponRecordId]
    * @Return com.buka.vo.CouponRecordVO
    */
    private CouponRecordVO getCartCouponRecord(Long couponRecordId) {

        // 检查优惠券记录ID是否有效
        if (couponRecordId == null || couponRecordId < 0) {
            return null;
        }

        // 发起远程调用优惠券微服务，获取优惠券信息
        JsonData couponRecordDetail = couponFeignService.getCouponRecordDetail(couponRecordId);

        // 检查远程调用是否成功
        if (couponRecordDetail.getCode() != 0) {
            log.error("[优惠券微服务]-获取优惠券信息失败");
            throw new RuntimeException("[优惠券微服务]-获取优惠券信息失败");
        }

        // 将获取的优惠券信息转换为CouponRecordVO对象
        CouponRecordVO couponRecordVO = couponRecordDetail.getData(new TypeReference<CouponRecordVO>() {
        });

        // 检查优惠券状态是否为已使用
        if (couponRecordVO.getUseState().equals(CouponStateEnum.USED.name())) {
            log.error("[优惠券微服务]-优惠券状态异常");
            throw new RuntimeException("[优惠券微服务]-优惠券状态异常");
        }

        // 检查优惠券是否在有效期内
        long currentTimestamp = System.currentTimeMillis();
        long end = couponRecordVO.getEndTime().getTime();
        long start = couponRecordVO.getStartTime().getTime();
        if (currentTimestamp < start || currentTimestamp > end) {
            log.error("[优惠券微服务]-优惠券状态异常");
            throw new RuntimeException("[优惠券微服务]-优惠券状态异常");
        }

        return couponRecordVO;
    }

    /**
    * @Author: lhb
    * @Description: 查询订单状态
    * @DateTime: 下午8:28 2025/3/13
    * @Params: [outTradeNo]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData queryProductOrderState(String outTradeNo) {
        LambdaQueryWrapper<ProductOrderDO> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProductOrderDO::getOutTradeNo,outTradeNo);
        ProductOrderDO one = getOne(lambdaQueryWrapper);
        return one==null?JsonData.buildError("订单不存在"):JsonData.buildSuccess(one.getState());
    }

    /**
    * @Author: lhb
    * @Description: 关闭产品订单
     * 该方法根据外部交易号（outTradeNo）查询订单，并根据订单状态决定是否关闭订单。
     * 如果订单不存在，直接返回true。
     * 如果订单已经支付，直接返回true。
     * 如果订单未支付，则向第三方支付查询订单状态，并根据查询结果更新订单状态。
    * @DateTime: 下午4:22 2025/3/18
    * @Params: [orderMessage]
    * @Return boolean
    */
    @Override
    public boolean closeProductOrder(String outTradeNo) {
        //根据外部交易号查询订单
        LambdaQueryWrapper<ProductOrderDO> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProductOrderDO::getOutTradeNo,outTradeNo);
        ProductOrderDO prodOrderDO = getOne(lambdaQueryWrapper);
        //如果订单不存在，直接返回true
        if (prodOrderDO == null){
            log.info("订单不存在");
            return true;
        }
        //如果订单已经支付，记录日志并返回true
        if (prodOrderDO.getState().equals(ProductOrderStateEnum.PAY.name())){
            log.info("直接确认消息,订单已经支付:{}");
            return true;
        }
        // 订单存在但未支付，向第三方支付查询订单状态
        PayInfoDTO payInfoDTO = new PayInfoDTO();
        payInfoDTO.setOutTradeNo(outTradeNo);
        payInfoDTO.setPayType(prodOrderDO.getPayType());
        String flag = payFactory.queryPaySuccess(payInfoDTO);
        //payResult==null表示未支付，payResult!=null表示已支付
        String payResult = flag;
        // 如果支付结果为空，表示未支付成功，取消本地订单
        if (StringUtils.isBlank(payResult)) {
            log.info("取消订单");
            LambdaUpdateWrapper<ProductOrderDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
            lambdaUpdateWrapper.set(ProductOrderDO::getState, ProductOrderStateEnum.CANCEL.name());
            update(lambdaUpdateWrapper);
            return true;
        }else {
            // 如果支付结果不为空，表示已支付，更新订单状态为已支付
            LambdaUpdateWrapper<ProductOrderDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
            lambdaUpdateWrapper.set(ProductOrderDO::getState, ProductOrderStateEnum.PAY.name());
            update(lambdaUpdateWrapper);
            return true;
        }

    }

    /**
    * @Author: lhb
    * @Description: 处理订单回调消息，根据交易状态更新订单状态
    * @DateTime: 下午3:27 2025/3/24
    * @Params: [paramsMap]
    * @Return boolean
    */
    @Override
    public boolean handlerOrderCallbackMsg(Map<String, String> paramsMap) {
        //从参数映射中获取订单号和交易状态
        String out_trade_no = paramsMap.get("out_trade_no");
        String trade_status = paramsMap.get("trade_status");

        //如果交易成功，更新订单状态
        if (trade_status.equals("TRADE_SUCCESS")) {
            LambdaUpdateWrapper<ProductOrderDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(ProductOrderDO::getOutTradeNo, out_trade_no);
            lambdaUpdateWrapper.set(ProductOrderDO::getState, ProductOrderStateEnum.PAY.name());
            update(lambdaUpdateWrapper);
            return true;
        }
        return false;
    }
}
