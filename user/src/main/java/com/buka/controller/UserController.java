package com.buka.controller;


import com.buka.dto.UserRegisterDto;
import com.buka.service.UserService;
import com.buka.util.JsonData;
import com.buka.enums.BizCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lhb
 * @since 2025-02-09
 */
@RestController
@RequestMapping("/api/user/v1")
public class UserController {


    @Autowired
    private UserService userService;


    /**
    * @Author: lhb
    * @Description: 上传头像
    * @DateTime: 下午2:05 2025/2/16
    * @Params: [multipartFile]
    * @Return com.buka.util.JsonData
    */
    @PostMapping("/upload")
    public JsonData upload(@RequestPart("file") MultipartFile multipartFile){
        String flag= userService.upload(multipartFile);
        return flag!=null?JsonData.buildSuccess(flag): JsonData.buildResult(BizCodeEnum.UPLOAD_ERROR);
    }


    /**
    * @Author: lhb
    * @Description: 注册
    * @DateTime: 下午2:06 2025/2/16
    * @Params: [userRegisterDto]
    * @Return com.buka.util.JsonData
    */
    @PostMapping("/register")
    public JsonData register(@RequestBody UserRegisterDto userRegisterDto){
        return userService.register(userRegisterDto);
    }

    /**
    * @Author: lhb
    * @Description: 登录
    * @DateTime: 下午2:37 2025/2/16
    * @Params: [userRegisterDto]
    * @Return com.buka.util.JsonData
    */
    @PostMapping("/login")
    public JsonData login(@RequestBody UserRegisterDto userRegisterDto){
        return userService.login(userRegisterDto);
    }

    /**
    * @Author: lhb
    * @Description: 查询个人信息
    * @DateTime: 下午3:33 2025/2/16
    * @Params: []
    * @Return com.buka.util.JsonData
    */
    @GetMapping("/info")
    public JsonData info(){
        return userService.info();
    }
}

