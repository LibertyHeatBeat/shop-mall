package com.buka.service;

import com.buka.dto.CartItemDto;
import com.buka.util.JsonData;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/2/18 下午12:07
 */
public interface CartService {
    JsonData addCart(CartItemDto cartItemDto);

    JsonData clearCart();

    JsonData myCart();

    JsonData changeCart(CartItemDto cartItemDto);

    JsonData deleteCart(long productId);
}
