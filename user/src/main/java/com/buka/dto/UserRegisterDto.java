package com.buka.dto;

import lombok.Data;

/**
 * @Description 用户注册数据传输对象
 * @Author lhb
 * @Date 2025/2/16
 */
@Data
public class UserRegisterDto {
    private String name;
    private String pwd;
    private String headImg;
    private String mail;
    private String code;
}
