package com.buka.vo;

import lombok.Data;

/**
 * @author lhb
 * @version 1.0
 * @description: 返回对象
 * @date 2025/2/16 下午3:08
 */
@Data
public class LoginUser {

    /**
     * 主键
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 头像
     */

    private String headImg;

    /**
     * 邮箱
     */
    private String mail;
}
