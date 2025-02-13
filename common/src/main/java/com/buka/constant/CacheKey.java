package com.buka.constant;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName CacheKey
 * @date 2025/2/10 15:35
 */


public class CacheKey {
    /**
     * 注册验证码，第一个是类型，第二个是接收号码
     */
    public static final String CHECK_CODE_KEY = "code:%s";


    /**
     * 购物车 hash 结果，key是用户唯一标识
     */
    public static final String CART_KEY = "cart:%s";


    /**
     * 提交表单的token key
     */
    public static final String SUBMIT_ORDER_TOKEN_KEY = "order:submit:%s";
}
