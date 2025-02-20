package com.buka.controller;


import com.alibaba.fastjson.JSON;
import com.buka.dto.ConfirmOrderDto;
import com.buka.enums.BizCodeEnum;
import com.buka.enums.ClientType;
import com.buka.enums.ProductOrderPayTypeEnum;
import com.buka.service.ProductOrderService;
import com.buka.util.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

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
    @PostMapping("/confirm")
    public void confirmOrder(@RequestBody ConfirmOrderDto confirmOrderDto, HttpServletResponse response) {
        JsonData jsonData = productOrderService.confirmOrder(confirmOrderDto);
        if (jsonData.getCode() == 0) {
            String client = confirmOrderDto.getClientType();
            String payType = confirmOrderDto.getPayType();
            if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.ALIPAY.name())) {
                if (client.equalsIgnoreCase(ClientType.H5.name())) {
                    //h5支付
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
}

