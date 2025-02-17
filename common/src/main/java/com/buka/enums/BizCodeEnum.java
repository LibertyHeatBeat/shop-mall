package com.buka.enums;

import lombok.Getter;

/**
 *
 * @author: lhb
 * @Description 状态码定义约束，共6位数，前三位代表服务，后4位代表接口
 *  比如 商品服务210,购物车是220、用户服务230，403代表权限
 *
 **/
public enum  BizCodeEnum {


    /**
     * 通用操作码
     */
    OPS_REPEAT(110001,"重复操作"),

    /**
     *验证码
     */
    CODE_TO_ERROR(240001,"接收号码不合规"),
    CODE_LIMITED(240002,"验证码发送过快"),
    CODE_ERROR(240003,"验证码错误"),
    CODE_CAPTCHA(240101,"图形验证码错误"),

    /**
     * 账号
     */
    ACCOUNT_REPEAT(250001,"账号已经存在"),
    ACCOUNT_UNREGISTER(250002,"账号不存在"),
    ACCOUNT_PWD_ERROR(250003,"账号或者密码错误"),
    UPLOAD_ERROR(250004, "上传失败"),
    /**
     * 优惠卷
     */
    COUPON_NOT_EXIST(250005,"优惠券不存在"),
    COUPON_NO_STOCK(25006,"优惠券库存不足"),
    COUPON_GET_FAIL(25007,"优惠券领取失败"),
    COUPON_OUT_OF_TIME(25008,"优惠券已过期"),
    COUPON_OUT_OF_LIMIT(25009,"优惠券超出领取限制"),
    NOT_LOGIN(25010, "未登录");

    @Getter
    private String message;
    @Getter
    private int code;

    private BizCodeEnum(int code,String message){
        this.code=code;
        this.message=message;
    }
}