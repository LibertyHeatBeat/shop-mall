package com.buka.controller;

import com.buka.dto.CartItemDto;
import com.buka.service.CartService;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lhb
 * @version 1.0
 * @description: 购物车
 * @date 2025/2/18 下午12:07
 */
@RestController
@RequestMapping("/api/cart/v1")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
    * @Author: lhb
    * @Description: 添加购物车
    * @DateTime: 下午12:12 2025/2/18
    * @Params: [cartItemDto]
    * @Return com.buka.util.JsonData
    */
    @PostMapping("/add")
    public JsonData addCart(@RequestBody CartItemDto cartItemDto) {
        return cartService.addCart(cartItemDto);
    }

    /**
    * @Author: lhb
    * @Description: 清空购物车
    * @DateTime: 上午8:56 2025/2/19
    * @Params: []
    * @Return com.buka.util.JsonData
    */
    @DeleteMapping("/clear")
    public JsonData clearCart() {
        return cartService.clearCart();
    }

    /**
    * @Author: lhb
    * @Description: 查看购物车
    * @DateTime: 上午8:57 2025/2/19
    * @Params: []
    * @Return com.buka.util.JsonData
    */
    @GetMapping("/myCart")
    public JsonData myCart() {
        return cartService.myCart();
    }

    /**
    * @Author: lhb
    * @Description: 修改购物车
    * @DateTime: 上午9:03 2025/2/19
    * @Params: [cartItemDto]
    * @Return com.buka.util.JsonData
    */
    @PutMapping("/change")
    public JsonData changeCart(@RequestBody CartItemDto cartItemDto) {
        return cartService.changeCart(cartItemDto);
    }

    /**
    * @Author: lhb
    * @Description: 删除购物车中的指定商品
    * @DateTime: 上午9:05 2025/2/19
    * @Params: [productId]
    * @Return com.buka.util.JsonData
    */
    @DeleteMapping("/delete/{product_id}")
    public JsonData deleteCart(@PathVariable("product_id") long productId) {
        return cartService.deleteCart(productId);
    }

    @PostMapping("/confirm_order_cart_items")
    public JsonData confirmOrderCartItems(@RequestBody List<Long> productIdList) {
        return cartService.confirmOrderCartItems(productIdList);
    }
}
