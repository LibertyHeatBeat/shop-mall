package com.buka.vo;

import java.math.BigDecimal;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/9 下午7:52
 */
public class CartItemVO {
    // 商品id
    private Long productId;
    //购买数量
    private Integer buyNum;
    //商品标题
    private String productTitle;
    //商品图片
    private String productImage;
    //商品价格
    private BigDecimal productPrice;
    //商品总价
    private BigDecimal totalAmount;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getBuyNum() {
        return buyNum;
    }

    public void setBuyNum(Integer buyNum) {
        this.buyNum = buyNum;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public BigDecimal getTotalAmount() {
        // 乘以购买数量
        BigDecimal multiply = this.productPrice.multiply(new BigDecimal(this.buyNum));
        return multiply;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
