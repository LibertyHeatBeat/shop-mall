package com.buka.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.buka.constant.CacheKey;
import com.buka.enums.BizCodeEnum;
import com.buka.service.MailService;
import com.buka.service.NotifyService;
import com.buka.util.CheckUtil;
import com.buka.util.CommonUtil;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @description 验证码服务实现类
 */
@Service
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 验证码的主题
     */
    private static final String SUBJECT= "buka验证码";

    /**
     * 验证码的内容
     */
    private static final String CONTENT= "您的验证码是%s,有效时间是60秒,请不要告诉其他人";

    /**
     * 验证码10分钟有效
     */
    private static final int CODE_EXPIRED = 60 * 1000 * 10;

    @Autowired
    private MailService mailService;

    /**
     * 发送验证码
     * @param email
     * @return
     */
    @Override
    public JsonData sendCode(String email) {
        // 生成Redis中存储验证码的key，格式为：check_code:email
        String key = String.format(CacheKey.CHECK_CODE_KEY, email);
        // 从Redis中获取该邮箱对应的验证码信息
        String s = (String) redisTemplate.opsForValue().get(key);
        // 如果Redis中存在验证码信息
        if (StringUtils.isNotBlank(s)) {
            // 解析验证码信息，获取验证码生成的时间戳
            long ttl = Long.parseLong(s.split("_")[1]);
            // 判断验证码是否在60秒内重复发送
            if (System.currentTimeMillis() - ttl < 60000) {
                // 如果在60秒内重复发送，返回验证码发送频率限制的错误
                return JsonData.buildResult(BizCodeEnum.CODE_LIMITED);
            }
        }
        // 生成一个6位随机验证码
        String code = CommonUtil.getRandomCode(6);
        // 将验证码和当前时间戳拼接，存入Redis，并设置过期时间为10分钟
        redisTemplate.opsForValue().set(key, code + "_" + System.currentTimeMillis(), CODE_EXPIRED, TimeUnit.MILLISECONDS);
        // 检查邮箱格式是否合法
        if (CheckUtil.isEmail(email)) {
            // 如果邮箱合法，发送验证码邮件
            mailService.sendSimpleMail(email, SUBJECT, String.format(CONTENT, code));
            // 返回操作成功的响应
            return JsonData.buildSuccess();
        } else {
            // 如果邮箱不合法，返回邮箱格式错误的响应
            return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
        }
    }

    /**
    * @Author: lhb
    * @Description: 验证邮箱和验证码是否匹配
    * @DateTime: 下午2:08 2025/2/16
    * @Params: [mail, code]
    * @Return boolean
    */
    @Override
    public boolean checkCode(String mail, String code) {
        String key = String.format(CacheKey.CHECK_CODE_KEY, mail);
        String s = (String) redisTemplate.opsForValue().get(key);

        // 检查缓存的验证码是否存在且不为空
        if (StringUtils.isNotBlank(s)) {
            // 比较用户输入的验证码和缓存的验证码是否一致
            if (code.equals(s.split("_")[0])) {
                // 验证码匹配，删除redis中的验证码信息
                redisTemplate.delete(key);
                return true;
            }
        }

        // 验证码不匹配或已过期，返回false
        return false;
    }
}
