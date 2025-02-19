package com.buka.service.impl;

import com.buka.dto.ConfirmOrderDto;
import com.buka.model.ProductOrderDO;
import com.buka.mapper.ProductOrderMapper;
import com.buka.service.ProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.util.JsonData;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lhb
 * @since 2025-02-19
 */
@Service
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrderDO> implements ProductOrderService {

    @Override
    public JsonData confirmOrder(ConfirmOrderDto confirmOrderDto) {
        return null;
    }
}
