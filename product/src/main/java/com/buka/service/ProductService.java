package com.buka.service;

import com.buka.model.ProductDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lhb
 * @since 2025-02-17
 */
public interface ProductService extends IService<ProductDO> {

    Object pageProduct(Long page, Long size);

    Object detail(Long productId);
}
