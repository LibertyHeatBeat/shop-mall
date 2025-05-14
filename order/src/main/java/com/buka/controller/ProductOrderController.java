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
    * @Params:
    *   confirmOrderDto - 包含订单确认信息的DTO对象，包括客户端类型、支付类型、总金额等
    *   response - HttpServletResponse对象，用于向客户端返回响应
    * @Return void
    */
    @PostMapping("/confirm")
    public void confirmOrder(@RequestBody ConfirmOrderDto confirmOrderDto, HttpServletResponse response) {
        // 调用服务层确认订单，并获取返回的JsonData对象
        JsonData jsonData = productOrderService.confirmOrder(confirmOrderDto);
        // 如果订单确认成功
        if (jsonData.getCode() == 0) {
            String client = confirmOrderDto.getClientType();
            String payType = confirmOrderDto.getPayType();

            // 根据支付类型进行不同的处理
            if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.ALIPAY.name())) {
                // 支付宝支付
                if (client.equalsIgnoreCase(ClientType.H5.name())) {
                    // H5支付
                    PayInfoDTO payInfoDTO = new PayInfoDTO();
                    payInfoDTO.setClientType(client);
                    payInfoDTO.setPayFee(confirmOrderDto.getTotalAmount());
                    payInfoDTO.setTitle("buka商城");
                    payInfoDTO.setOutTradeNo(jsonData.getData().toString());
                    payInfoDTO.setPayType(confirmOrderDto.getPayType());

                    // 调用支付工厂生成支付页面
                    String from = payFactory.pay(payInfoDTO);

                    // 将支付页面信息存入Redis，用于重新支付
                    redisTemplate.opsForValue().set(jsonData.getData().toString(), from, 14, TimeUnit.MINUTES);

                    // 返回支付页面给客户端
                    writeData(response, from);
                } else if (client.equalsIgnoreCase(ClientType.APP.name())) {
                    // APP支付
                } else if (client.equalsIgnoreCase(ClientType.PC.name())) {
                    // PC支付
                }
            } else if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.WECHAT.name())) {
                // 微信支付
            } else if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.BANK.name())) {
                // 银行卡支付
            }
        } else {
            // 订单创建失败时的处理
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
     * @Description: 重新支付方法。该方法用于根据订单号查询支付状态，并返回相应的支付结果。
     *               如果订单号在Redis中存在对应的支付信息，则返回成功信息；否则返回订单支付超时的错误信息。
     * @DateTime: 下午1:52 2025/3/24
     * @Params: outTradeNo - 订单号，用于查询支付状态。
     * @Return: JsonData - 返回支付结果的JSON数据。如果支付信息存在，返回成功信息；否则返回支付超时错误信息。
     */
    @GetMapping("repay")
    public JsonData repay(@RequestParam("out_trade_no") String outTradeNo) {
        // 从Redis中获取订单号对应的支付信息
        String from = redisTemplate.opsForValue().get(outTradeNo);

        // 如果支付信息存在，返回成功信息
        if (from != null) {
            return JsonData.buildSuccess(from);
        }

        // 如果支付信息不存在，返回订单支付超时的错误信息
        return JsonData.buildResult(BizCodeEnum.ORDER_PAY_TIME_OUT);
    }


    /**
     * @Author: lhb
     * @Description: 生成并返回一个用于支付提交的Token，以防止重复提交。该Token会被存储在Redis中，有效期为20分钟。
     * @DateTime: 下午3:52 2025/3/24
     * @Params: [] 无参数
     * @Return com.buka.util.JsonData 返回一个包含生成的Token的JsonData对象
     */
    @GetMapping("get_token")
    public JsonData getToken() {
        // 生成一个32位的随机字符串作为Token
        String stringNumRandom = CommonUtil.getStringNumRandom(32);

        // 从线程局部变量中获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        // 根据用户ID生成Redis中的Key
        String key= String.format(CacheKey.SUBMIT_ORDER_TOKEN_KEY, loginUser.getId());

        // 将生成的Token存储到Redis中，并设置有效期为20分钟
        redisTemplate.opsForValue().set(key, stringNumRandom, 20, TimeUnit.MINUTES);

        // 返回包含生成的Token的JsonData对象
        return JsonData.buildSuccess(stringNumRandom);
    }

}

