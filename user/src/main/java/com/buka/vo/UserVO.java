package com.buka.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author lhb
 * @version 1.0
 * @description: 用户返回数据类
 * @date 2025/2/16 下午3:35
 */
@Data
public class UserVO {
    /**
     * 昵称
     */
    private String name;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 头像
     */
    private String headImg;

    /**
     * 用户签名
     */
    private String slogan;

    /**
     * 0表示女，1表示男
     */
    private Integer sex;

    /**
     * 积分
     */
    private Integer points;

    private Date createTime;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 盐，用于个人敏感信息处理
     */
    private String secret;

}
