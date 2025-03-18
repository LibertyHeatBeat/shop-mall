package com.buka.pay;

import com.buka.dto.PayInfoDTO;

/**
 * @author lhb
 * @version 1.0
 * @description: 支付策略接口开发
 * @date 2025/3/18 下午5:45
 */
public interface PayStrategy {
    /**
     * 下单
     * @param payInfoDTO
     * @return
     */
    String unifiedorder(PayInfoDTO payInfoDTO);


    /**
     *  退款
     * @param payInfoDTO
     * @return
     */
    default String refund(PayInfoDTO payInfoDTO){return "";}


    /**
     * 查询支付是否成功
     * @param payInfoDTO
     * @return
     */
    default String queryPaySuccess(PayInfoDTO payInfoDTO){return "";}
}
