package com.buka.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description MybatisPlus分页插件配置
 * @Author lhb
 * @Date 2025/2/15
 */
@Configuration
public class MybatisPlusPageConfig {
    /**
    * @Author: lhb
    * @Description: 新的分页插件, 一缓和二缓遵循mybatis的规则, 3.4.0(3.4.0)
     * *需要设置 MybatisConfiguration#useDeprecatedExecutor = false 避免缓存出现问题
    * @DateTime: 上午9:48 2025/2/15
    * @Params: []
    * @Return MybatisPlusInterceptor
    */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
