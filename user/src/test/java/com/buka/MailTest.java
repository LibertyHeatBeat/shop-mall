package com.buka;


import com.buka.service.MailService;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
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

    @Test
    public void sendMailWithQiniuyun(){
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region1());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
//...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
        String accessKey = "Kj9b4N4bHst_5HRKJ68srRsejUJSi6ijPVoT2N-b";
        String secretKey = "tsXLfEiw_jobeAMl8VV1chmZP-Z0tZvpybyCcwCH";
        String bucket = "b0404-shop-mall";
//如果是Windows情况下，格式是 D:\\qiniu\\test.png
        String localFilePath = "C:\\Users\\lhb20\\Videos\\NARAKA  BLADEPOINT\\NARAKA  BLADEPOINT Screenshot 2023.02.26 - 14.48.26.54.png";
//默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            Response response = uploadManager.put(localFilePath, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            ex.printStackTrace();
            if (ex.response != null) {
                System.err.println(ex.response);

                try {
                    String body = ex.response.toString();
                    System.err.println(body);
                } catch (Exception ignored) {
                }
            }
        }

    }
}

