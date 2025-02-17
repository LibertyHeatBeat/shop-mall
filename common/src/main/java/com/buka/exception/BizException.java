package com.buka.exception;

import com.buka.enums.BizCodeEnum;
import lombok.Data;

/**
 * @Description 全局异常处理
 * @Author lhb
 * @Date 2025/2/15
 */
@Data
public class BizException extends RuntimeException{
    private Integer code;
    private String msg;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.msg = message;
    }

    public BizException(BizCodeEnum bizCodeEnum) {
        super(bizCodeEnum.getMessage());
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMessage();
    }
}
