package com.buka.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.buka.dto.UserRegisterDto;
import com.buka.enums.BizCodeEnum;
import com.buka.fegin.CouponFeignService;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.UserDO;
import com.buka.mapper.UserMapper;
import com.buka.oss.QinNiuOss;
import com.buka.request.NewUserCouponRequest;
import com.buka.service.NotifyService;
import com.buka.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.util.CommonUtil;
import com.buka.util.JWTUtil;
import com.buka.util.JsonData;
import com.buka.vo.LoginUser;
import com.buka.vo.UserVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lhb
 * @since 2025-02-09
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private QinNiuOss qinNiuOss;
    @Autowired
    private NotifyService notifyService;

    /**
    * @Author: lhb
    * @Description: 上传文件到七牛云存储
    * @DateTime: 下午1:24 2025/2/16
    * @Params: [multipartFile]
    * @Return java.lang.String
    */
    @Override
    public String upload(MultipartFile multipartFile) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String format = dateTimeFormatter.format(now);
        String fileName = CommonUtil.generateUUID();
        String originalFilename = multipartFile.getOriginalFilename();
        int i = originalFilename.lastIndexOf(".");
        String substring = originalFilename.substring(i);
        String name=format+"/"+fileName+substring;
        qinNiuOss.QiNiuUpload(multipartFile,name);
        return "http://srlmydmt4.hb-bkt.clouddn.com/"+name;
    }

    /**
    * @Author: lhb
    * @Description: 注册方法实现
    * @DateTime: 下午2:06 2025/2/16
    * @Params: [userRegisterDto]
    * @Return com.buka.util.JsonData
    */
    @Override
    @GlobalTransactional
    public JsonData register(UserRegisterDto userRegisterDto) {
        boolean checkCode = false;
        if (StringUtils.isNotBlank(userRegisterDto.getMail())) {
            checkCode = notifyService.checkCode(userRegisterDto.getMail(), userRegisterDto.getCode());
        }
        if (!checkCode) {
            return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userRegisterDto, userDO);
        userDO.setSecret("$1$"+CommonUtil.getStringNumRandom(8));
        String s = Md5Crypt.md5Crypt(userDO.getPwd().getBytes(), userDO.getSecret());
        userDO.setPwd(s);
        if (checkUnique(userRegisterDto.getMail())) {
            this.save(userDO);
            //TODO  发放优惠卷 远程调用
            NewUserCouponRequest newUserCouponRequest=new NewUserCouponRequest();
            newUserCouponRequest.setUserId(userDO.getId());
            newUserCouponRequest.setName(userDO.getName());
            JsonData jsonData= couponFeignService.newUserCoupon(newUserCouponRequest);
            return JsonData.buildSuccess(jsonData);
        } else {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_REPEAT);
        }
    }

    /**
    * @Author: lhb
    * @Description: 登录方法实现
    * @DateTime: 下午2:38 2025/2/16
    * @Params: [userRegisterDto]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData login(UserRegisterDto userRegisterDto) {
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getMail, userRegisterDto.getMail());
        UserDO user = this.getOne(lambdaQueryWrapper);
        if (user != null){
            if (user.getPwd().equals(Md5Crypt.md5Crypt(userRegisterDto.getPwd().getBytes(), user.getSecret()))) {
                LoginUser loginUser = new LoginUser();
                BeanUtils.copyProperties(user, loginUser);
                String jwt = JWTUtil.geneJsonWebToken(loginUser);
                return JsonData.buildSuccess(jwt);
            } else {
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
            }
        }
        return  JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
    }

    @Override
    public JsonData info() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        UserDO user = getById(loginUser.getId());
        if (user != null) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return JsonData.buildSuccess(userVO);
        }
        return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
    }

    /**
    * @Author: lhb
    * @Description: 检查邮箱是否唯一方法
     * 此方法用于检查给定的邮箱地址在数据库中是否唯一它通过查询用户表中具有相同邮箱地址的记录数来实现
     * 如果记录数大于0，则表示邮箱不唯一；如果为0，则表示邮箱唯一
    * @DateTime: 下午2:18 2025/2/16
    * @Params: [mail]
    * @Return boolean
    */
    private  boolean checkUnique(String mail) {
        // 创建LambdaQueryWrapper对象，用于构建查询条件
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        // 设置查询条件：where mail=?
        lambdaQueryWrapper.eq(UserDO::getMail,mail);
        // 执行查询，并返回结果记录数
        int count = this.count(lambdaQueryWrapper);
        // 根据查询结果判断邮箱是否唯一
        return count>0?false:true;
    }
}
