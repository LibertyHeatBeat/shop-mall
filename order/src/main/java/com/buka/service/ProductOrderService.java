package com.buka.service;

import com.buka.dto.ConfirmOrderDto;
import com.buka.model.OrderMessage;
import com.buka.model.ProductOrderDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buka.util.JsonData;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lhb
 * @since 2025-02-19
 */
public interface ProductOrderService extends IService<ProductOrderDO> {

    JsonData confirmOrder(ConfirmOrderDto confirmOrderDto);

    JsonData queryProductOrderState(String outTradeNo);

    boolean closeProductOrder(String outTradeNo);
}
