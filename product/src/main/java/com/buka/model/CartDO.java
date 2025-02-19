package com.buka.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description: TODO 
 * @author lhb
 * @date 2025/2/18 下午12:05
 * @version 1.0
 */
public class CartDO {
    // 购物车中商品的总数量
    private Integer totalNum;
    // 购物车中商品的总价格
    private BigDecimal totalAmount;
    // 购物车中商品的优惠价格
    private BigDecimal realPayAmount;
    // 购物车中商品的列表
    private List<CartItemDO> cartItem;




    public Integer getTotalNum() {
        if ( this.cartItem!=null){
            return this.totalNum=this.cartItem.size();
        }
        return 0;
    }

    public void setTotalNum(Integer totalNum) {
        this.totalNum = totalNum;
    }

    public BigDecimal getTotalAmount() {
        // 购物车中商品的总价格
        BigDecimal bigDecimal=new BigDecimal(0);
        // 遍历购物车中商品的列表
        for (CartItemDO cartItemDO : cartItem) {
            bigDecimal=bigDecimal.add(cartItemDO.getTotalAmount());
        }
        return bigDecimal;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getRealPayAmount() {
        // 购物车中商品的总价格
        BigDecimal bigDecimal=new BigDecimal(0);
        // 遍历购物车中商品的列表
        for (CartItemDO cartItemDO : cartItem) {
            bigDecimal=bigDecimal.add(cartItemDO.getTotalAmount());
        }
        return bigDecimal;
    }

    public void setRealPayAmount(BigDecimal realPayAmount) {
        this.realPayAmount = realPayAmount;
    }

    public List<CartItemDO> getCartItem() {
        return cartItem;
    }

    public void setCartItem(List<CartItemDO> cartItem) {
        this.cartItem = cartItem;
    }
}
