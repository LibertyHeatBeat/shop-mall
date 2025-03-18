package com.buka.pay;

import com.buka.dto.PayInfoDTO;

/**
 * @author lhb
 * @version 1.0
 * @description: 策略封装类
 * @date 2025/3/18 下午5:49
 */
public class PayStrategyContext {

    private PayStrategy payStrategy;

    /**
    * @Author: lhb
    * @Description:
    * @DateTime: 下午5:51 2025/3/18
    * @Params: [payStrategy]
    * @Return
    */
    public PayStrategyContext(PayStrategy payStrategy) {
        this.payStrategy = payStrategy;
    }

    /**
    * @Author: lhb
    * @Description: 根据支付策略，调用不同的支付
    * @DateTime: 下午5:50 2025/3/18
    * @Params: [payInfoDTO]
    * @Return java.lang.String
    */
    public String executeUnifiedorder(PayInfoDTO payInfoDTO){
        return this.payStrategy.unifiedorder(payInfoDTO);
    }

    /**
    * @Author: lhb
    * @Description: 根据支付的策略，调用不同的查询订单支持状态
    * @DateTime: 下午5:51 2025/3/18
    * @Params: [payInfoDTO]
    * @Return java.lang.String
    */
    public String executeQueryPaySuccess(PayInfoDTO payInfoDTO){
        return this.payStrategy.queryPaySuccess(payInfoDTO);

    }
}
