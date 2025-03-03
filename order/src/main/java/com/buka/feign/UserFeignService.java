package com.buka.feign;

import com.buka.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/9 下午7:43
 */
@FeignClient("user-service")
public interface UserFeignService {


    @GetMapping("/api/address/v1/find/{address_id}")
    JsonData find(@PathVariable("address_id") Long id);
}
