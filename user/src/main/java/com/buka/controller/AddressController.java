package com.buka.controller;


import com.buka.dto.AddressAddDto;
import com.buka.enums.BizCodeEnum;
import com.buka.service.AddressService;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 电商-公司收发货地址表 前端控制器
 * </p>
 *
 * @author lhb
 * @since 2025-02-09
 */
@RestController
@RequestMapping("/api/address/v1")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /**
    * @Author: lhb
    * @Description: 添加地址
    * @DateTime: 下午6:59 2025/2/16
    * @Params: [addressAddDto]
    * @Return com.buka.util.JsonData
    */
    @PostMapping("/add")
    public JsonData add(@RequestBody AddressAddDto addressAddDto){
        return addressService.add(addressAddDto);
    }


    /**
    * @Author: lhb
    * @Description: 根据id查询地址
    * @DateTime: 下午6:59 2025/2/16
    * @Params: [id]
    * @Return com.buka.util.JsonData
    */
    @GetMapping("/find/{address_id}") //  api/arrdess/v1/find/1
    public JsonData find(@PathVariable("address_id")Long id){
        if (addressService.getById(id)!=null){
            return JsonData.buildSuccess(addressService.getById(id));
        }else {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
        }
    }


    /**
    * @Author: lhb
    * @Description: 根据地址id删除地址
    * @DateTime: 下午6:59 2025/2/16
    * @Params: [id]
    * @Return com.buka.util.JsonData
    */
    @DeleteMapping("/del/{address_id}")
    public JsonData del(@PathVariable("address_id")Long id){
        return addressService.remo(id);
    }

    /**
    * @Author: lhb
    * @Description: 查询全部地址
    * @DateTime: 下午6:59 2025/2/16
    * @Params: []
    * @Return com.buka.util.JsonData
    */
    @GetMapping("/list")
    public JsonData findUserAllAddress(){
        return  addressService.findUserAllAddress();
    }

}

