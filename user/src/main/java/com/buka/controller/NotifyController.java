package com.buka.controller;

import com.buka.enums.BizCodeEnum;
import com.buka.service.NotifyService;
import com.buka.util.CommonUtil;
import com.buka.util.JsonData;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

/**
 * @author lhb
 * @version 1.0
 * @ClassName NotifyController
 * @date 2025/2/10 9:55
 */

@RestController
@RequestMapping("/api/notify/v1")
@Slf4j
public class NotifyController {


    @Autowired
    private Producer producer;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private NotifyService notifyService;

    /**
     * 验证码过期时间
     * 临时使用10分钟有效，方便测试
     */
    private static final long CAPTCHA_CODE_EXPIRED = 60 * 1000 * 10;

    /**
     * @description:返回验证码图片
     * @author: lhb
     * @date: 2025/2/10 9:54
     * @param: No such property: code for class: Script1
     * @return:
     **/
    @GetMapping("/captcha")// get能访问
    public void captcha(HttpServletResponse response, HttpServletRequest request){
        //1,生成验证码答案
        String text = producer.createText();
        log.info("验证码答案：{}",text);

        //1.1保存进redis   将电脑的id+浏览器信息充当redis 的key
        String key = getCaptchhakey(request);
        redisTemplate.opsForValue().set(key,text,CAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);


        //2,生成图片
        BufferedImage image = producer.createImage(text);
        ServletOutputStream os=null;

        try {
            //获取输出流
            os=response.getOutputStream();
            //将图片写入输出流
            ImageIO.write(image,"jpg",os);
            os.flush();
            os.close();

        }catch (Exception e){
            log.info("返回图片出错：{}",e);
        }
    }


    /**
     * @description:根据浏览器信息配置获取验证码的Redis存储的key
     * @param request
     * @return
     */
    public String getCaptchhakey(HttpServletRequest request){
        //1,获取ip地址
        String ip = CommonUtil.getIpAddr(request);
        //2,获取浏览器信息
        String header = request.getHeader("User-Agent");

        return "user-service:captcha:"+CommonUtil.MD5(ip+header);
    }

    /**
     * @description:向邮箱发送验证码并验证
     * @param email
     * @param captcha
     * @param request
     * @return
     */
    @GetMapping("/send_codes")
    public JsonData sendCode(@RequestParam(value = "email", required = true) String email,
                             @RequestParam(value = "captcha", required = true) String captcha,
                             HttpServletRequest request){
        //1,获取redis中的验证码
        String key = getCaptchhakey(request);
        String code = redisTemplate.opsForValue().get(key);
        log.info(email+captcha);
        //2,验证验证码
        if (code != null && captcha != null && code.equalsIgnoreCase(captcha)) {
            redisTemplate.delete(key);
            JsonData jsonData= notifyService.sendCode(email);
            return jsonData;
        } else {
            return JsonData.buildResult(BizCodeEnum.CODE_CAPTCHA);
        }
    }

}

