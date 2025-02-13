package com.buka;


import com.buka.UserServiceApplication;
import com.buka.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName MailTest
 * @date 2025/2/10 14:31
 */

@SpringBootTest(classes = UserServiceApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class MailTest {

    @Autowired
    private MailService mailService;

    @Test
    public void sendMail() {
        mailService.sendSimpleMail("2406702019@qq.com","buka","你好好学习了");

    }
}

