package com.buka.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.buka.dto.AddressAddDto;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.AddressDO;
import com.buka.mapper.AddressMapper;
import com.buka.service.AddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.util.JsonData;
import com.buka.vo.AddressVo;
import com.buka.vo.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 电商-公司收发货地址表 服务实现类
 * </p>
 *
 * @author lhb
 * @since 2025-02-09
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressDO> implements AddressService {

    /**
    * @Author: lhb
    * @Description: 添加新的地址信息，并处理默认地址
    * @DateTime: 下午6:56 2025/2/16
    * @Params: [addressAddDto]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData add(AddressAddDto addressAddDto) {
        //准备数据
        AddressDO addressDO = new AddressDO();
        BeanUtils.copyProperties(addressAddDto, addressDO);
        addressDO.setUserId(LoginInterceptor.threadLocal.get().getId());
        addressDO.setCreateTime(new Date());

        //检查是否设置为默认地址
        if (addressAddDto.getDefaultStatus()==1){
            //这是默认地址
            //更新当前用户的其他默认地址，将它们的默认状态取消
            LambdaUpdateWrapper<AddressDO> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(AddressDO::getUserId,LoginInterceptor.threadLocal.get().getId());
            lambdaUpdateWrapper.eq(AddressDO::getDefaultStatus,1);
            lambdaUpdateWrapper.set(AddressDO::getDefaultStatus,0);
            update(lambdaUpdateWrapper);
        }

        //保存地址信息
        boolean save = save(addressDO);
        //返回成功响应
        return JsonData.buildSuccess();
    }

    /**
    * @Author: lhb
    * @Description:  删除指定ID的地址，并处理默认地址
    * @DateTime: 下午6:57 2025/2/16
    * @Params: [id]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData remo(Long id) {
        // 根据ID获取地址对象
        AddressDO addressDO = getById(id);

        // 检查并更新默认地址
        if (addressDO.getDefaultStatus()==1){
            // 默认地址
            LambdaQueryWrapper<AddressDO> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(AddressDO::getUserId,LoginInterceptor.threadLocal.get().getId());
            lambdaQueryWrapper.eq(AddressDO::getDefaultStatus,0);

            // 获取用户的所有非默认地址
            List<AddressDO> list = list(lambdaQueryWrapper);
            if (list!=null&& list.size()!=0){
                // 将第一个非默认地址设置为默认地址
                AddressDO addressDO1 = list.get(0);
                addressDO1.setDefaultStatus(1);
                updateById(addressDO1);
            }
        }

        // 删除地址
        removeById(id);
        // 返回成功结果
        return JsonData.buildSuccess();
    }

    /**
    * @Author: lhb
    * @Description: 获取当前登录用户的所有地址信息
    * @DateTime: 下午6:57 2025/2/16
    * @Params: []
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData findUserAllAddress() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        LambdaQueryWrapper<AddressDO> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AddressDO::getUserId,loginUser.getId());
        List<AddressDO> list = list(lambdaQueryWrapper);

        List<AddressVo> collect = list.stream().map(obg -> {
            AddressVo addressVo = new AddressVo();
            BeanUtils.copyProperties(obg, addressVo);
            return addressVo;
        }).collect(Collectors.toList());

        return JsonData.buildSuccess(collect);
    }
}
