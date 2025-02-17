package com.buka.oss;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;

/**
 * @Description
 * @Author lhb
 * @Date 2025/2/14
 */
@Component
public class QinNiuOss {
    @Value("${ak}")
    private String accessKey;

    @Value("${sk}")
    private String secretKey;

    @Value("${bucket}")
    private String bucket;

    public void QiNiuUpload(MultipartFile multipartFile, String name) {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region1());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
//...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
//        String accessKey = "i8IL8qR2Scxe6lq-ugLqVf2o__tCaoicvR7PDupv";
//        String secretKey = "EOYIkBgkiC_cAYG4o9dGCylZkB5Z8TnTTsx8vdDz";
//        String bucket = "404-shop1";

//默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = name;

        try {



            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);

            try {
                Response response = uploadManager.put(multipartFile.getBytes(), key, upToken);
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
        } catch (Exception ex) {
            //ignore
        }

    }
}
