package com.buka.service;

import com.buka.dto.UserRegisterDto;
import com.buka.model.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buka.util.JsonData;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lhb
 * @since 2025-02-09
 */
public interface UserService extends IService<UserDO> {

    String upload(MultipartFile multipartFile);

    JsonData register(UserRegisterDto userRegisterDto);

    JsonData login(UserRegisterDto userRegisterDto);

    JsonData info();
}
