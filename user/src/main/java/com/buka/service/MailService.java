package com.buka.service;

/**
 * @author: lhb
 * 邮件服务
 **/
public interface MailService {
    void sendSimpleMail(String mail, String subject, String content);
}
