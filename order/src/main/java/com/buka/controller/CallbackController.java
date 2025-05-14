package com.buka.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.buka.service.ProductOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author lhb
 * @version 1.0
 * @description: 接受支付回调消息
 * @date 2025/3/24 下午3:14
 */
@RestController
@Slf4j
@RequestMapping("/api/callback/v1")
public class CallbackController {
    @Value("${aibabaPay.alipayPublicKey}")
    private String ALIPAY_PUBLIC_KEY;
    @Autowired
    private ProductOrderService productOrderService;

    /**
    * @Author: lhb
    * @Description: 处理支付宝回调消息请求
    *              该函数接收支付宝的回调请求，验证请求的签名，并根据验证结果处理订单回调消息。
    *              如果签名验证成功且订单处理成功，则返回"success"；否则返回"failure"。
    * @DateTime: 下午3:29 2025/3/24
    * @Params: [request] HttpServletRequest对象，包含支付宝回调请求的所有参数。
    * @Return java.lang.String 返回处理结果，"success"表示处理成功，"failure"表示处理失败。
    */
    @PostMapping("/alipay")
    public String allipay(HttpServletRequest request){
        // 将请求参数转换为Map，方便后续处理
        Map<String,String> paramsMap =  convertRequestParamsToMap(request);
        log.info("支付宝回调通知结果:{}",paramsMap);

        // 初始化签名验证标志
        boolean signVerified = false;
        try {
            // 使用支付宝公钥验证请求的签名
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, ALIPAY_PUBLIC_KEY, "UTF-8", "RSA2");

            // 如果签名验证成功，处理订单回调消息
            if (signVerified) {
                System.out.println("签名验证成功");
                boolean flag = productOrderService.handlerOrderCallbackMsg(paramsMap);
                if (flag){
                    return "success";
                }
            } else {
                // 签名验证失败，返回"failure"
                System.out.println("签名验证失败");
                return "failure";
            }
        } catch (AlipayApiException e) {
            // 捕获并打印异常信息
            e.printStackTrace();
        }
        // 默认返回"failure"
        return "failure";
    }


    /**
    * @Author: lhb
    * @Description: 将HttpServletRequest对象中的请求参数转换为Map<String, String>格式。
    *               如果某个参数有多个值，则将其值设置为空字符串。
    * @DateTime: 下午3:29 2025/3/24
    * @Params: [request] HttpServletRequest对象，包含客户端请求的参数。
    * @Return java.util.Map<java.lang.String,java.lang.String> 返回一个Map，其中键为参数名，值为参数值。
    *         如果参数有多个值，则值为空字符串。
    */
    private Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {
        // 初始化一个空的HashMap用于存储转换后的参数
        Map<String, String> params = new HashMap<>();

        // 获取请求参数的所有键值对
        Set<Map.Entry<String, String[]>> entries = request.getParameterMap().entrySet();

        // 遍历所有参数键值对
        for (Map.Entry<String, String[]> entry : entries) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            int size = values.length;

            // 如果参数只有一个值，则直接放入Map；否则，将值设置为空字符串
            if (size == 1) {
                params.put(name, values[0]);
            } else {
                params.put(name, "");
            }
        }

        // 返回转换后的参数Map
        return params;
    }

}
