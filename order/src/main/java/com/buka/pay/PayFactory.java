package com.buka.pay;

import com.buka.dto.PayInfoDTO;
import com.buka.enums.ProductOrderPayTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author lhb
 * @version 1.0
 * @description: 支付工厂类，用于根据支付类型选择相应的支付策略并执行支付操作。
 * @date 2025/3/18 下午5:51
 */
@Component
@Slf4j
public class PayFactory {
    /**
     * @Author: lhb
     * @Description: 根据支付类型选择相应的支付策略并执行支付操作。
     * @DateTime: 下午5:52 2025/3/18
     * @Params: [payInfoDTO] 支付信息数据传输对象，包含支付类型等必要信息。
     * @Return java.lang.String 返回支付操作的结果，通常为支付订单号或支付状态信息。
     */
    public String pay(PayInfoDTO payInfoDTO){
        String payType = payInfoDTO.getPayType();

        // 根据支付类型选择相应的支付策略
        if(ProductOrderPayTypeEnum.ALIPAY.name().equalsIgnoreCase(payType)){
            AlipayStrategy alipayStrategy = new AlipayStrategy();
            // 使用支付宝策略执行支付操作
            PayStrategyContext payStrategyContext = new PayStrategyContext(alipayStrategy);

            return payStrategyContext.executeUnifiedorder(payInfoDTO);

        } else if(ProductOrderPayTypeEnum.WECHAT.name().equalsIgnoreCase(payType)){
            WechatPayStrategy wechatPayStrategy = new WechatPayStrategy();
            // 使用微信策略执行支付操作（暂未实现）
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);

            return payStrategyContext.executeUnifiedorder(payInfoDTO);
        }

        // 如果支付类型不支持，返回空字符串
        return "";
    }
}
