package com.buka.service.impl;


import com.buka.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务实现类
 */
@Service
@Slf4j
public class MailServiceImpl implements MailService {
    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.from}")
    private String from;

    /**
    * @Author: lhb
    * @Description: 发送邮件
    * @DateTime: 下午2:37 2025/3/12
    * @Params: [mail, subject, content]
    * @Return void
    */
    @Override
    public void sendSimpleMail(String mail, String subject, String content) {
        // 创建一个SimpleMailMessage对象，用于封装邮件信息
        SimpleMailMessage message = new SimpleMailMessage();
        // 设置邮件发送方地址，从配置文件中读取
        message.setFrom(from);
        // 设置邮件接收方地址
        message.setTo(mail);
        // 设置邮件主题
        message.setSubject(subject);
        // 设置邮件内容
        message.setText(content);
        // 使用JavaMailSender发送邮件
        javaMailSender.send(message);
        // 记录日志，表示邮件发送成功，并输出邮件信息
        log.info("邮件发成功:{}", message.toString());
    }
}
