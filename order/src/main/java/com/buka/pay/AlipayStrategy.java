package com.buka.pay;

import com.buka.dto.PayInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author lhb
 * @version 1.0
 * @description: 支付宝支付具体策略
 * @date 2025/3/18 下午5:46
 */
@Slf4j
@Service
public class AlipayStrategy implements PayStrategy {
    @Override
    public String unifiedorder(PayInfoDTO payInfoDTO) {
        return null;
    }

    @Override
    public String refund(PayInfoDTO payInfoDTO) {
        return PayStrategy.super.refund(payInfoDTO);
    }

    @Override
    public String queryPaySuccess(PayInfoDTO payInfoDTO) {
        return PayStrategy.super.queryPaySuccess(payInfoDTO);
    }
}
