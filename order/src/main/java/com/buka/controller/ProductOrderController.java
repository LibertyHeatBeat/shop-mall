package com.buka.controller;


import com.alibaba.fastjson.JSON;
import com.buka.constant.CacheKey;
import com.buka.dto.ConfirmOrderDto;
import com.buka.dto.PayInfoDTO;
import com.buka.enums.BizCodeEnum;
import com.buka.enums.ClientType;
import com.buka.enums.ProductOrderPayTypeEnum;
import com.buka.interceptor.LoginInterceptor;
import com.buka.pay.PayFactory;
import com.buka.service.ProductOrderService;
import com.buka.util.CommonUtil;
import com.buka.util.JsonData;
import com.buka.vo.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lhb
 * @since 2025-02-19
 */
@RestController
@RequestMapping("/api/productOrder/v1")
@Slf4j
public class ProductOrderController {
    @Autowired
    private ProductOrderService productOrderService;
    @Autowired
    private PayFactory payFactory;
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
    * @Author: lhb
    * @Description: 提交订单
    * @DateTime: 下午4:31 2025/3/3
    * @Params: [confirmOrderDto, response]
    * @Return void
    */
    @PostMapping("/confirm")
    public void confirmOrder(@RequestBody ConfirmOrderDto confirmOrderDto, HttpServletResponse response) {
        JsonData jsonData = productOrderService.confirmOrder(confirmOrderDto);
        if (jsonData.getCode() == 0) {
            String client = confirmOrderDto.getClientType();
            String payType = confirmOrderDto.getPayType();
            if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.ALIPAY.name())) {
                //支付宝支付
                if (client.equalsIgnoreCase(ClientType.H5.name())) {
                    //h5支付
                    PayInfoDTO payInfoDTO = new PayInfoDTO();
                    payInfoDTO.setClientType(client);
                    payInfoDTO.setPayFee(confirmOrderDto.getTotalAmount());
                    payInfoDTO.setTitle("buka商城");
                    payInfoDTO.setOutTradeNo(jsonData.getData().toString());
                    payInfoDTO.setPayType(confirmOrderDto.getPayType());


                    String from = payFactory.pay(payInfoDTO);

                    //将支付页面放到redis  用于重新支付
                    redisTemplate.opsForValue().set(jsonData.getData().toString(),from, 14, TimeUnit.MINUTES);
                    // 返回支付页面
                    writeData(response,from);
                }else if (client.equalsIgnoreCase(ClientType.APP.name())) {
                    //app支付
                } else if (client.equalsIgnoreCase(ClientType.PC.name())) {
                    //pc支付
                }
            } else if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.WECHAT.name())) {
                //微信支付
            } else if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.BANK.name())) {
                //银行卡支付
            }

        } else {
            //创建订单失败
            try {
                // 记录错误日志并返回错误信息
                log.error("创建订单失败{}", jsonData.toString());
                response.setContentType("test/json;charset=utf8");
                response.getWriter().write(JSON.toJSONString(JsonData.buildResult(BizCodeEnum.ORDER_ERROR)));
                response.getWriter().flush();
                response.getWriter().close();
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
    }

    /**
    * @Author: lhb
    * @Description: 写出支付页面的HTML
    * @DateTime: 下午1:47 2025/3/24
    * @Params: [response, from]
    * @Return void
    */
    private void writeData(HttpServletResponse response, String from) {
        response.setContentType("text/html;charset=utf8");
        try {
            response.getWriter().write(from);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            log.error("写出Html异常：{}", e);
        }

    }

    /**
    * @Author: lhb
    * @Description: 查询订单状态
    * @DateTime: 下午4:47 2025/3/9
    * @Params: [outTradeNo]
    * @Return com.buka.util.JsonData
    */
    @GetMapping("query_state")
    public JsonData queryProductOrderState(@RequestParam("out_trade_no") String outTradeNo) {
        return productOrderService.queryProductOrderState(outTradeNo);
    }
    /**
    * @Author: lhb
    * @Description: 重新支付方法
    * @DateTime: 下午1:52 2025/3/24
    * @Params:
    * @Return
    */
    @GetMapping("repay")
    public JsonData repay(@RequestParam("out_trade_no") String outTradeNo) {
        String from = redisTemplate.opsForValue().get(outTradeNo);
        if (from != null) {
            return JsonData.buildSuccess(from);
        }
        return JsonData.buildResult(BizCodeEnum.ORDER_PAY_TIME_OUT);
    }

    /**
    * @Author: lhb
    * @Description: 对支付发放token防止重复提交
    * @DateTime: 下午3:52 2025/3/24
    * @Params: []
    * @Return com.buka.util.JsonData
    */
    @GetMapping("get_token")
    public JsonData getToken() {
        String stringNumRandom = CommonUtil.getStringNumRandom(32);
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String key= String.format(CacheKey.SUBMIT_ORDER_TOKEN_KEY, loginUser.getId());
        redisTemplate.opsForValue().set(key, stringNumRandom, 20, TimeUnit.MINUTES);
        return JsonData.buildSuccess(stringNumRandom);
    }
}

