package com.buka.controller;


import com.buka.model.AddressDO;
import com.buka.service.AddressService;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 电商-公司收发货地址表 前端控制器
 * </p>
 *
 * @author lhb
 * @since 2025-02-09
 */
@RestController
@RequestMapping("/addressDO")
public class AddressController {

    @Autowired
    private AddressService addressService;




    @RequestMapping("/test")
    public JsonData test(){
        List<AddressDO> list =null;
        try {
            list = addressService.list();
        }catch (Exception e){
            //出错了
            return JsonData.buildCodeAndMsg(29932,"导出失败");
        }

        return JsonData.buildSuccess(list);
    }
}

